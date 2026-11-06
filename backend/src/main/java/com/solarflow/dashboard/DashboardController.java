package com.solarflow.dashboard;

import com.solarflow.common.security.CurrentUser;
import com.solarflow.lead.domain.Lead;
import com.solarflow.lead.domain.LeadRepository;
import com.solarflow.payment.domain.Payment;
import com.solarflow.payment.domain.PaymentRepository;
import com.solarflow.project.domain.Project;
import com.solarflow.project.domain.ProjectRepository;
import com.solarflow.quotation.domain.Quotation;
import com.solarflow.quotation.domain.QuotationPricing;
import com.solarflow.quotation.domain.QuotationRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DashboardController {

  private final LeadRepository leads;
  private final QuotationRepository quotations;
  private final ProjectRepository projects;
  private final PaymentRepository payments;
  private final CurrentUser currentUser;

  public DashboardController(
      LeadRepository leads,
      QuotationRepository quotations,
      ProjectRepository projects,
      PaymentRepository payments,
      CurrentUser currentUser
  ) {
    this.leads = leads;
    this.quotations = quotations;
    this.projects = projects;
    this.payments = payments;
    this.currentUser = currentUser;
  }

  @GetMapping("/dashboard")
  @Transactional(readOnly = true)
  public DashboardView dashboard() {
    return build(true);
  }

  @GetMapping("/reports")
  @Transactional(readOnly = true)
  public DashboardView reports() {
    return build(false);
  }

  private DashboardView build(boolean limitLists) {
    UUID companyId = currentUser.require().companyId();
    List<Lead> leadRows = leads.findByCompanyIdOrderByCreatedAtDesc(companyId);
    List<Quotation> quoteRows = quotations.findByCompanyIdOrderByCreatedAtDesc(companyId);
    List<Project> projectRows = projects.findByCompanyIdOrderByCreatedAtDesc(companyId);
    List<Payment> paymentRows = payments.findByCompanyIdOrderByCreatedAtDesc(companyId);
    long won = leadRows.stream().filter(lead -> lead.getStatus() == Lead.Status.WON).count();
    long lost = leadRows.stream().filter(lead -> lead.getStatus() == Lead.Status.LOST).count();
    long open = leadRows.size() - won - lost;
    BigDecimal received = sum(paymentRows, Payment.Status.RECEIVED);
    BigDecimal pending = sum(paymentRows, Payment.Status.PENDING);
    BigDecimal billed = quoteRows.stream().filter(quote -> quote.getStatus() == Quotation.Status.ACCEPTED)
        .map(Quotation::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal outstanding = billed.subtract(received).max(BigDecimal.ZERO);
    YearMonth month = YearMonth.now(ZoneOffset.UTC);
    BigDecimal collectedThisMonth = paymentRows.stream()
        .filter(payment -> payment.getStatus() == Payment.Status.RECEIVED && payment.getPaidAt() != null)
        .filter(payment -> YearMonth.from(payment.getPaidAt().atZone(ZoneOffset.UTC)).equals(month))
        .map(Payment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return new DashboardView(
        new Kpis(open, quoteRows.stream().filter(quote -> quote.getStatus() == Quotation.Status.DRAFT || quote.getStatus() == Quotation.Status.SENT).count(),
            projectRows.stream().filter(project -> project.getStatus() == Project.Status.PLANNED || project.getStatus() == Project.Status.IN_PROGRESS).count(),
            QuotationPricing.money(outstanding), QuotationPricing.money(collectedThisMonth), QuotationPricing.money(pending)),
        revenue(paymentRows),
        new Conversion(won, lost, open),
        projectRows.stream().filter(project -> project.getStatus() != Project.Status.CANCELLED)
            .limit(limitLists ? 5 : 20)
            .map(project -> new ProgressItem(project.getId(), project.getName(), project.getProgressPercent(), project.getStatus()))
            .toList(),
        leadRows.stream().limit(limitLists ? 5 : 20).map(lead -> new LeadItem(lead.getId(), lead.getContactName(), lead.getStatus(), lead.getPriority(), lead.getFollowUpOn())).toList(),
        quoteRows.stream().filter(quote -> quote.getStatus() == Quotation.Status.DRAFT || quote.getStatus() == Quotation.Status.SENT)
            .limit(limitLists ? 5 : 20)
            .map(quote -> new QuoteItem(quote.getId(), quote.getNumber(), quote.getStatus(), quote.getTotalAmount()))
            .toList(),
        projectRows.stream().filter(project -> project.getTargetDate() != null && !project.getTargetDate().isBefore(LocalDate.now()))
            .limit(limitLists ? 5 : 20)
            .map(project -> new ScheduleItem(project.getId(), project.getName(), project.getTargetDate(), project.getStatus()))
            .toList(),
        paymentRows.stream().filter(payment -> payment.getStatus() == Payment.Status.PENDING)
            .limit(limitLists ? 5 : 20)
            .map(payment -> new PaymentItem(payment.getId(), payment.getCustomerId(), payment.getAmount(), payment.getStatus()))
            .toList()
    );
  }

  private static List<MonthPoint> revenue(List<Payment> paymentRows) {
    YearMonth start = YearMonth.now(ZoneOffset.UTC).minusMonths(5);
    List<MonthPoint> points = new ArrayList<>();
    for (int index = 0; index < 6; index++) {
      YearMonth point = start.plusMonths(index);
      BigDecimal amount = paymentRows.stream()
          .filter(payment -> payment.getStatus() == Payment.Status.RECEIVED && payment.getPaidAt() != null)
          .filter(payment -> YearMonth.from(payment.getPaidAt().atZone(ZoneOffset.UTC)).equals(point))
          .map(Payment::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      points.add(new MonthPoint(point.toString(), QuotationPricing.money(amount)));
    }
    return points;
  }

  private static BigDecimal sum(List<Payment> rows, Payment.Status status) {
    return rows.stream().filter(row -> row.getStatus() == status).map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public record Kpis(long openLeads, long pendingQuotations, long activeProjects, BigDecimal outstanding, BigDecimal collectedThisMonth, BigDecimal pendingPayments) {
  }

  public record MonthPoint(String month, BigDecimal amount) {
  }

  public record Conversion(long won, long lost, long open) {
  }

  public record ProgressItem(UUID id, String name, int progress, Project.Status status) {
  }

  public record LeadItem(UUID id, String contactName, Lead.Status status, Lead.Priority priority, LocalDate followUpOn) {
  }

  public record QuoteItem(UUID id, String number, Quotation.Status status, BigDecimal totalAmount) {
  }

  public record ScheduleItem(UUID id, String name, LocalDate targetDate, Project.Status status) {
  }

  public record PaymentItem(UUID id, UUID customerId, BigDecimal amount, Payment.Status status) {
  }

  public record DashboardView(
      Kpis kpis,
      List<MonthPoint> revenueByMonth,
      Conversion leadConversion,
      List<ProgressItem> installationProgress,
      List<LeadItem> recentLeads,
      List<QuoteItem> pendingQuotations,
      List<ScheduleItem> upcomingInstallations,
      List<PaymentItem> pendingPayments
  ) {
  }
}
