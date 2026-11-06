package com.solarflow.company;

import com.solarflow.company.domain.CompanyRepository;
import com.solarflow.customer.domain.Customer;
import com.solarflow.customer.domain.CustomerRepository;
import com.solarflow.inventory.domain.Product;
import com.solarflow.inventory.domain.ProductRepository;
import com.solarflow.lead.domain.Lead;
import com.solarflow.lead.domain.LeadRepository;
import com.solarflow.notification.domain.AppNotification;
import com.solarflow.notification.domain.AppNotificationRepository;
import com.solarflow.payment.domain.Payment;
import com.solarflow.payment.domain.PaymentRepository;
import com.solarflow.project.domain.Project;
import com.solarflow.project.domain.ProjectRepository;
import com.solarflow.project.domain.ProjectTask;
import com.solarflow.project.domain.ProjectTaskRepository;
import com.solarflow.quotation.domain.Quotation;
import com.solarflow.quotation.domain.QuotationCounterRepository;
import com.solarflow.quotation.domain.QuotationLine;
import com.solarflow.quotation.domain.QuotationLineRepository;
import com.solarflow.quotation.domain.QuotationRepository;
import com.solarflow.service.domain.ServiceRequest;
import com.solarflow.service.domain.ServiceRequestRepository;
import com.solarflow.user.domain.UserAccount;
import com.solarflow.user.domain.UserAccountRepository;
import com.solarflow.warranty.domain.Warranty;
import com.solarflow.warranty.domain.WarrantyRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataSeeder {

  private final boolean enabled;
  private final CompanyRepository companies;
  private final UserAccountRepository users;
  private final PasswordEncoder passwordEncoder;
  private final CustomerRepository customers;
  private final LeadRepository leads;
  private final ProductRepository products;
  private final QuotationRepository quotations;
  private final QuotationLineRepository lines;
  private final QuotationCounterRepository counters;
  private final ProjectRepository projects;
  private final ProjectTaskRepository tasks;
  private final PaymentRepository payments;
  private final WarrantyRepository warranties;
  private final ServiceRequestRepository serviceRequests;
  private final AppNotificationRepository notifications;

  public DemoDataSeeder(
      @Value("${solarflow.demo-data:false}") boolean enabled,
      CompanyRepository companies,
      UserAccountRepository users,
      PasswordEncoder passwordEncoder,
      CustomerRepository customers,
      LeadRepository leads,
      ProductRepository products,
      QuotationRepository quotations,
      QuotationLineRepository lines,
      QuotationCounterRepository counters,
      ProjectRepository projects,
      ProjectTaskRepository tasks,
      PaymentRepository payments,
      WarrantyRepository warranties,
      ServiceRequestRepository serviceRequests,
      AppNotificationRepository notifications
  ) {
    this.enabled = enabled;
    this.companies = companies;
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.customers = customers;
    this.leads = leads;
    this.products = products;
    this.quotations = quotations;
    this.lines = lines;
    this.counters = counters;
    this.projects = projects;
    this.tasks = tasks;
    this.payments = payments;
    this.warranties = warranties;
    this.serviceRequests = serviceRequests;
    this.notifications = notifications;
  }

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void seed() {
    if (!enabled || users.existsByEmailIgnoreCase("owner@helios.example")) {
      return;
    }
    var company = companies.findBySlug("helios").orElseThrow();
    UserAccount owner = member(company.getId(), "owner@helios.example", "Asha", "Rao", UserAccount.Role.OWNER);
    UserAccount sales = member(company.getId(), "sales@helios.example", "Vikram", "Joshi", UserAccount.Role.SALES);
    UserAccount technician = member(company.getId(), "tech@helios.example", "Neha", "Kulkarni", UserAccount.Role.TECHNICIAN);

    Customer meera = customer("Meera Iyer", "9876500101", "meera.iyer@example.com", "Baner", "Walk-in");
    Customer rohan = customer("Rohan Deshmukh", "9876500102", "rohan.deshmukh@example.com", "Nagpur", "Referral");
    customers.save(meera);
    customers.save(rohan);

    Product panel = new Product();
    panel.setCompanyId(company.getId());
    panel.setSku("PNL-540");
    panel.setName("540 W mono panel");
    panel.setCategory(Product.Category.PANEL);
    panel.setBrand("Helios");
    panel.setModel("H540");
    panel.setUnit("nos");
    panel.setUnitPrice(new BigDecimal("14500.00"));
    panel.setWattage(540);
    panel.setWarrantyYears(25);
    panel.setStockQuantity(new BigDecimal("18.00"));
    panel.setReorderLevel(new BigDecimal("6.00"));
    panel.setActive(true);
    products.save(panel);

    Lead won = lead(company.getId(), meera.getId(), "Meera Iyer", Lead.Status.WON, Lead.Priority.HIGH, sales.getId(), LocalDate.now().minusDays(12));
    Lead open = lead(company.getId(), null, "Sanjay Patil", Lead.Status.NEW, Lead.Priority.MEDIUM, sales.getId(), LocalDate.now().plusDays(1));
    leads.save(won);
    leads.save(open);

    Quotation quote = new Quotation();
    quote.setCompanyId(company.getId());
    quote.setNumber("QT-00001");
    quote.setCustomerId(meera.getId());
    quote.setLeadId(won.getId());
    quote.setStatus(Quotation.Status.ACCEPTED);
    quote.setSystemCapacityKw(new BigDecimal("3.24"));
    quote.setPanelCount(6);
    quote.setSubtotal(new BigDecimal("87000.00"));
    quote.setDiscountAmount(new BigDecimal("2000.00"));
    quote.setTaxRate(new BigDecimal("18.00"));
    quote.setTaxAmount(new BigDecimal("15300.00"));
    quote.setTotalAmount(new BigDecimal("100300.00"));
    quote.setValidUntil(LocalDate.now().plusDays(15));
    quote.setCreatedBy(owner.getId());
    quotations.save(quote);
    QuotationLine line = new QuotationLine();
    line.setCompanyId(company.getId());
    line.setQuotationId(quote.getId());
    line.setProductId(panel.getId());
    line.setDescription("540 W mono panel");
    line.setQuantity(new BigDecimal("6.00"));
    line.setUnitPrice(new BigDecimal("14500.00"));
    line.setLineTotal(new BigDecimal("87000.00"));
    lines.save(line);
    counters.findById(company.getId()).ifPresent(counter -> counter.setNextValue(2));

    Project project = new Project();
    project.setCompanyId(company.getId());
    project.setCustomerId(meera.getId());
    project.setQuotationId(quote.getId());
    project.setName("Baner 3.24 kW rooftop");
    project.setStatus(Project.Status.IN_PROGRESS);
    project.setCapacityKw(new BigDecimal("3.24"));
    project.setSiteAddress("12 Baner Road, Pune");
    project.setStartDate(LocalDate.now().minusDays(4));
    project.setTargetDate(LocalDate.now().plusDays(6));
    project.setLeadTechnicianId(technician.getId());
    project.setProgressPercent(45);
    projects.save(project);
    ProjectTask task = new ProjectTask();
    task.setCompanyId(company.getId());
    task.setProjectId(project.getId());
    task.setTitle("Mount the structure");
    task.setStatus(ProjectTask.Status.IN_PROGRESS);
    task.setAssignedTo(technician.getId());
    task.setDueOn(LocalDate.now().plusDays(2));
    task.setSortOrder(1);
    tasks.save(task);

    Payment received = payment(company.getId(), meera.getId(), project.getId(), "40000.00", Payment.Status.RECEIVED, Instant.now().minus(2, ChronoUnit.DAYS));
    Payment pending = payment(company.getId(), meera.getId(), project.getId(), "60300.00", Payment.Status.PENDING, null);
    payments.save(received);
    payments.save(pending);

    Warranty warranty = new Warranty();
    warranty.setCompanyId(company.getId());
    warranty.setCustomerId(meera.getId());
    warranty.setProjectId(project.getId());
    warranty.setProductId(panel.getId());
    warranty.setWarrantyType(Warranty.Type.PANEL);
    warranty.setStartDate(LocalDate.now().minusMonths(1));
    warranty.setEndDate(LocalDate.now().plusDays(40));
    warranty.setStatus(Warranty.Status.EXPIRING);
    warranty.setCoverageNotes("Manufacturing defects on the modules.");
    warranties.save(warranty);

    ServiceRequest request = new ServiceRequest();
    request.setCompanyId(company.getId());
    request.setCustomerId(rohan.getId());
    request.setTitle("Inverter restarting at noon");
    request.setDescription("The inverter trips when the load peaks.");
    request.setPriority(ServiceRequest.Priority.HIGH);
    request.setStatus(ServiceRequest.Status.OPEN);
    request.setOpenedAt(Instant.now().minus(1, ChronoUnit.DAYS));
    serviceRequests.save(request);

    AppNotification notification = new AppNotification();
    notification.setCompanyId(company.getId());
    notification.setUserId(owner.getId());
    notification.setTitle("Follow up with Sanjay Patil");
    notification.setBody("The Baner enquiry is still waiting on a first call.");
    notification.setCategory("LEAD");
    notifications.save(notification);
  }

  private UserAccount member(java.util.UUID companyId, String email, String first, String last, UserAccount.Role role) {
    UserAccount user = new UserAccount();
    user.setCompanyId(companyId);
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode("Solarflow#2026"));
    user.setFirstName(first);
    user.setLastName(last);
    user.setRole(role);
    user.setStatus(UserAccount.Status.ACTIVE);
    return users.save(user);
  }

  private Customer customer(String name, String phone, String email, String city, String source) {
    Customer customer = new Customer();
    customer.setCompanyId(companies.findBySlug("helios").orElseThrow().getId());
    customer.setDisplayName(name);
    customer.setPhone(phone);
    customer.setEmail(email);
    customer.setCity(city);
    customer.setState("Maharashtra");
    customer.setStatus(Customer.Status.ACTIVE);
    customer.setSource(source);
    return customer;
  }

  private Lead lead(java.util.UUID companyId, java.util.UUID customerId, String name, Lead.Status status, Lead.Priority priority, java.util.UUID assignee, LocalDate followUp) {
    Lead lead = new Lead();
    lead.setCompanyId(companyId);
    lead.setCustomerId(customerId);
    lead.setContactName(name);
    lead.setStatus(status);
    lead.setPriority(priority);
    lead.setAssignedTo(assignee);
    lead.setFollowUpOn(followUp);
    lead.setSource("Website");
    lead.setEstimatedCapacityKw(new BigDecimal("3.00"));
    return lead;
  }

  private Payment payment(java.util.UUID companyId, java.util.UUID customerId, java.util.UUID projectId, String amount, Payment.Status status, Instant paidAt) {
    Payment payment = new Payment();
    payment.setCompanyId(companyId);
    payment.setCustomerId(customerId);
    payment.setProjectId(projectId);
    payment.setAmount(new BigDecimal(amount));
    payment.setMethod(Payment.Method.UPI);
    payment.setStatus(status);
    payment.setPaidAt(paidAt);
    payment.setReceiptNumber(status == Payment.Status.RECEIVED ? "RCP-DEMO001" : null);
    return payment;
  }
}
