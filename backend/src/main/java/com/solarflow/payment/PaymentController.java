package com.solarflow.payment;

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.solarflow.common.exception.NotFoundException;
import com.solarflow.common.security.CurrentUser;
import com.solarflow.company.domain.Company;
import com.solarflow.company.domain.CompanyRepository;
import com.solarflow.customer.domain.CustomerRepository;
import com.solarflow.payment.domain.Payment;
import com.solarflow.payment.domain.PaymentRepository;
import com.solarflow.quotation.domain.Quotation;
import com.solarflow.quotation.domain.QuotationPricing;
import com.solarflow.quotation.domain.QuotationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

  private final PaymentRepository payments;
  private final CustomerRepository customers;
  private final QuotationRepository quotations;
  private final CompanyRepository companies;
  private final CurrentUser currentUser;

  public PaymentController(
      PaymentRepository payments,
      CustomerRepository customers,
      QuotationRepository quotations,
      CompanyRepository companies,
      CurrentUser currentUser
  ) {
    this.payments = payments;
    this.customers = customers;
    this.quotations = quotations;
    this.companies = companies;
    this.currentUser = currentUser;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public PaymentPage list() {
    UUID companyId = companyId();
    List<Payment> rows = payments.findByCompanyIdOrderByCreatedAtDesc(companyId);
    BigDecimal received = sum(rows, Payment.Status.RECEIVED);
    BigDecimal pending = sum(rows, Payment.Status.PENDING);
    BigDecimal billed = quotations.findByCompanyIdAndStatus(companyId, Quotation.Status.ACCEPTED).stream()
        .map(Quotation::getTotalAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal outstanding = billed.subtract(received);
    if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
      outstanding = BigDecimal.ZERO;
    }
    return new PaymentPage(
        QuotationPricing.money(received),
        QuotationPricing.money(pending),
        QuotationPricing.money(outstanding),
        rows.stream().map(PaymentView::from).toList()
    );
  }

  @PostMapping
  @Transactional
  public PaymentView create(@Valid @RequestBody PaymentInput input) {
    if (customers.findByIdAndCompanyId(input.customerId(), companyId()).isEmpty()) {
      throw new NotFoundException();
    }
    if (input.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new com.solarflow.common.exception.BadRequestException("Amount must be greater than zero");
    }
    Payment payment = new Payment();
    payment.setCompanyId(companyId());
    payment.setCustomerId(input.customerId());
    payment.setProjectId(input.projectId());
    payment.setAmount(QuotationPricing.money(input.amount()));
    payment.setMethod(input.method());
    payment.setStatus(input.status());
    payment.setReferenceNo(input.referenceNo());
    payment.setNotes(input.notes());
    payment.setReceiptNumber("RCP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    if (input.status() == Payment.Status.RECEIVED) {
      payment.setPaidAt(Instant.now());
    }
    return PaymentView.from(payments.save(payment));
  }

  @GetMapping("/{id}/receipt")
  @Transactional(readOnly = true)
  public ResponseEntity<byte[]> receipt(@PathVariable UUID id) {
    Payment payment = payments.findByIdAndCompanyId(id, companyId()).orElseThrow(NotFoundException::new);
    Company company = companies.findById(payment.getCompanyId()).orElseThrow(NotFoundException::new);
    var customer = customers.findByIdAndCompanyId(payment.getCustomerId(), payment.getCompanyId()).orElseThrow(NotFoundException::new);
    byte[] pdf = render(company.getName(), customer.getDisplayName(), payment);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + payment.getReceiptNumber() + ".pdf\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }

  private static BigDecimal sum(List<Payment> rows, Payment.Status status) {
    return rows.stream().filter(row -> row.getStatus() == status).map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private static byte[] render(String company, String customer, Payment payment) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      Document document = new Document();
      PdfWriter.getInstance(document, output);
      document.open();
      document.add(new Paragraph(company, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
      document.add(new Paragraph("Payment receipt " + payment.getReceiptNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
      document.add(new Paragraph("Customer  " + customer));
      document.add(new Paragraph("Amount  INR " + payment.getAmount().toPlainString()));
      document.add(new Paragraph("Method  " + payment.getMethod()));
      document.add(new Paragraph("Status  " + payment.getStatus()));
      if (payment.getReferenceNo() != null) {
        document.add(new Paragraph("Reference  " + payment.getReferenceNo()));
      }
      document.close();
      return output.toByteArray();
    } catch (Exception exception) {
      throw new IllegalStateException("Could not render the receipt", exception);
    }
  }

  private UUID companyId() {
    return currentUser.require().companyId();
  }

  public record PaymentInput(
      @NotNull UUID customerId,
      UUID projectId,
      @NotNull BigDecimal amount,
      @NotNull Payment.Method method,
      @NotNull Payment.Status status,
      @Size(max = 64) String referenceNo,
      @Size(max = 500) String notes
  ) {
  }

  public record PaymentView(
      UUID id, UUID customerId, UUID projectId, BigDecimal amount, Payment.Method method, Payment.Status status,
      String referenceNo, String receiptNumber, Instant paidAt, String notes
  ) {
    static PaymentView from(Payment payment) {
      return new PaymentView(
          payment.getId(), payment.getCustomerId(), payment.getProjectId(), payment.getAmount(), payment.getMethod(),
          payment.getStatus(), payment.getReferenceNo(), payment.getReceiptNumber(), payment.getPaidAt(), payment.getNotes()
      );
    }
  }

  public record PaymentPage(BigDecimal received, BigDecimal pending, BigDecimal outstanding, List<PaymentView> items) {
  }
}
