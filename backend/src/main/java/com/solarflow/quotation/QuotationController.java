package com.solarflow.quotation;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.solarflow.common.exception.BadRequestException;
import com.solarflow.common.exception.NotFoundException;
import com.solarflow.common.security.CurrentUser;
import com.solarflow.company.domain.Company;
import com.solarflow.company.domain.CompanyRepository;
import com.solarflow.customer.domain.CustomerRepository;
import com.solarflow.inventory.domain.Product;
import com.solarflow.inventory.domain.ProductRepository;
import com.solarflow.lead.domain.Lead;
import com.solarflow.lead.domain.LeadRepository;
import com.solarflow.project.domain.Project;
import com.solarflow.project.domain.ProjectRepository;
import com.solarflow.quotation.domain.Quotation;
import com.solarflow.quotation.domain.QuotationCounter;
import com.solarflow.quotation.domain.QuotationCounterRepository;
import com.solarflow.quotation.domain.QuotationLine;
import com.solarflow.quotation.domain.QuotationLineRepository;
import com.solarflow.quotation.domain.QuotationPricing;
import com.solarflow.quotation.domain.QuotationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
@RequestMapping("/api/v1/quotations")
public class QuotationController {

  private final QuotationRepository quotations;
  private final QuotationLineRepository lines;
  private final QuotationCounterRepository counters;
  private final ProductRepository products;
  private final CompanyRepository companies;
  private final CustomerRepository customers;
  private final LeadRepository leads;
  private final ProjectRepository projects;
  private final CurrentUser currentUser;

  public QuotationController(
      QuotationRepository quotations,
      QuotationLineRepository lines,
      QuotationCounterRepository counters,
      ProductRepository products,
      CompanyRepository companies,
      CustomerRepository customers,
      LeadRepository leads,
      ProjectRepository projects,
      CurrentUser currentUser
  ) {
    this.quotations = quotations;
    this.lines = lines;
    this.counters = counters;
    this.products = products;
    this.companies = companies;
    this.customers = customers;
    this.leads = leads;
    this.projects = projects;
    this.currentUser = currentUser;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public List<QuotationView> list() {
    UUID companyId = companyId();
    return quotations.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
        .map(quote -> QuotationView.from(quote, lines.findByQuotationIdAndCompanyIdOrderByCreatedAtAsc(quote.getId(), companyId)))
        .toList();
  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public QuotationView get(@PathVariable UUID id) {
    Quotation quote = require(id);
    return QuotationView.from(quote, lines.findByQuotationIdAndCompanyIdOrderByCreatedAtAsc(quote.getId(), quote.getCompanyId()));
  }

  @PostMapping("/preview")
  public QuotationPricing.Result preview(@Valid @RequestBody QuoteInput input) {
    return price(input);
  }

  @PostMapping
  @Transactional
  public QuotationView create(@Valid @RequestBody QuoteInput input) {
    UUID companyId = companyId();
    QuotationPricing.Result priced = price(input);
    Quotation quote = new Quotation();
    quote.setCompanyId(companyId);
    quote.setNumber(nextNumber(companyId));
    quote.setCreatedBy(currentUser.require().id());
    apply(quote, input, priced);
    quotations.save(quote);
    saveLines(quote, input.lines());
    if (quote.getLeadId() != null) {
      leads.findByIdAndCompanyId(quote.getLeadId(), companyId).ifPresent(lead -> {
        if (lead.getStatus() != Lead.Status.WON && lead.getStatus() != Lead.Status.LOST) {
          lead.setStatus(Lead.Status.QUOTED);
        }
      });
    }
    return QuotationView.from(quote, lines.findByQuotationIdAndCompanyIdOrderByCreatedAtAsc(quote.getId(), companyId));
  }

  @PostMapping("/{id}/accept")
  @Transactional
  public QuotationView accept(@PathVariable UUID id) {
    Quotation quote = require(id);
    if (quote.getCustomerId() == null) {
      throw new BadRequestException("Link a customer before accepting the quotation");
    }
    quote.setStatus(Quotation.Status.ACCEPTED);
    if (quote.getLeadId() != null) {
      leads.findByIdAndCompanyId(quote.getLeadId(), quote.getCompanyId()).ifPresent(lead -> lead.setStatus(Lead.Status.WON));
    }
    boolean projectExists = projects.findByCompanyIdOrderByCreatedAtDesc(quote.getCompanyId()).stream()
        .anyMatch(project -> quote.getId().equals(project.getQuotationId()));
    if (!projectExists) {
      Project project = new Project();
      project.setCompanyId(quote.getCompanyId());
      project.setCustomerId(quote.getCustomerId());
      project.setQuotationId(quote.getId());
      project.setName(quote.getNumber() + " installation");
      project.setStatus(Project.Status.PLANNED);
      project.setCapacityKw(quote.getSystemCapacityKw());
      project.setProgressPercent(0);
      projects.save(project);
    }
    return QuotationView.from(quote, lines.findByQuotationIdAndCompanyIdOrderByCreatedAtAsc(quote.getId(), quote.getCompanyId()));
  }

  @GetMapping("/{id}/pdf")
  @Transactional(readOnly = true)
  public ResponseEntity<byte[]> pdf(@PathVariable UUID id) {
    Quotation quote = require(id);
    Company company = companies.findById(quote.getCompanyId()).orElseThrow(NotFoundException::new);
    var quoteLines = lines.findByQuotationIdAndCompanyIdOrderByCreatedAtAsc(quote.getId(), quote.getCompanyId());
    byte[] body = render(company, quote, quoteLines);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + quote.getNumber() + ".pdf\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(body);
  }

  private QuotationPricing.Result price(QuoteInput input) {
    UUID companyId = companyId();
    List<QuotationPricing.Line> pricedLines = new ArrayList<>();
    for (LineInput line : input.lines()) {
      BigDecimal unitPrice = line.unitPrice();
      if (line.productId() != null) {
        Product product = products.findByIdAndCompanyId(line.productId(), companyId).orElseThrow(NotFoundException::new);
        unitPrice = product.getUnitPrice();
      }
      pricedLines.add(new QuotationPricing.Line(line.quantity(), unitPrice));
    }
    return QuotationPricing.price(pricedLines, input.discountAmount(), input.taxRate());
  }

  private void apply(Quotation quote, QuoteInput input, QuotationPricing.Result priced) {
    UUID companyId = quote.getCompanyId();
    if (input.customerId() != null && customers.findByIdAndCompanyId(input.customerId(), companyId).isEmpty()) {
      throw new NotFoundException();
    }
    if (input.leadId() != null && leads.findByIdAndCompanyId(input.leadId(), companyId).isEmpty()) {
      throw new NotFoundException();
    }
    quote.setCustomerId(input.customerId());
    quote.setLeadId(input.leadId());
    quote.setSurveyId(input.surveyId());
    quote.setStatus(Quotation.Status.DRAFT);
    quote.setSystemCapacityKw(input.systemCapacityKw());
    quote.setPanelCount(input.panelCount());
    quote.setSubtotal(priced.subtotal());
    quote.setDiscountAmount(priced.discountAmount());
    quote.setTaxRate(priced.taxRate());
    quote.setTaxAmount(priced.taxAmount());
    quote.setTotalAmount(priced.total());
    quote.setValidUntil(input.validUntil());
    quote.setNotes(input.notes());
  }

  private void saveLines(Quotation quote, List<LineInput> inputs) {
    for (LineInput input : inputs) {
      BigDecimal unitPrice = input.unitPrice();
      String description = input.description().trim();
      if (input.productId() != null) {
        Product product = products.findByIdAndCompanyId(input.productId(), quote.getCompanyId()).orElseThrow(NotFoundException::new);
        unitPrice = product.getUnitPrice();
        if (description.isBlank()) {
          description = product.getName();
        }
      }
      QuotationLine line = new QuotationLine();
      line.setCompanyId(quote.getCompanyId());
      line.setQuotationId(quote.getId());
      line.setProductId(input.productId());
      line.setDescription(description);
      line.setQuantity(input.quantity());
      line.setUnitPrice(unitPrice);
      line.setLineTotal(QuotationPricing.lineTotal(input.quantity(), unitPrice));
      lines.save(line);
    }
  }

  private String nextNumber(UUID companyId) {
    QuotationCounter counter = counters.findForUpdate(companyId).orElseGet(() -> {
      QuotationCounter created = new QuotationCounter();
      created.setCompanyId(companyId);
      created.setNextValue(1);
      return counters.save(created);
    });
    long value = counter.getNextValue();
    counter.setNextValue(value + 1);
    return "QT-" + String.format("%05d", value);
  }

  private byte[] render(Company company, Quotation quote, List<QuotationLine> quoteLines) {
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      Document document = new Document(PageSize.A4, 48, 48, 56, 48);
      PdfWriter.getInstance(document, output);
      document.open();
      Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
      Font body = FontFactory.getFont(FontFactory.HELVETICA, 10);
      Font label = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
      document.add(new Paragraph(company.getLegalName() == null ? company.getName() : company.getLegalName(), title));
      document.add(new Paragraph(address(company), body));
      if (company.getGstin() != null) {
        document.add(new Paragraph("GSTIN " + company.getGstin(), body));
      }
      document.add(new Paragraph(" "));
      document.add(new Paragraph("Quotation " + quote.getNumber(), label));
      document.add(new Paragraph("Status " + quote.getStatus() + (quote.getValidUntil() == null ? "" : "  ·  Valid until " + quote.getValidUntil()), body));
      if (quote.getSystemCapacityKw() != null) {
        document.add(new Paragraph("System capacity " + quote.getSystemCapacityKw() + " kW", body));
      }
      document.add(new Paragraph(" "));
      PdfPTable table = new PdfPTable(new float[] {4, 1, 1.4f, 1.4f});
      table.setWidthPercentage(100);
      for (String heading : List.of("Description", "Qty", "Rate", "Amount")) {
        PdfPCell cell = new PdfPCell(new Phrase(heading, label));
        cell.setPadding(6);
        table.addCell(cell);
      }
      for (QuotationLine line : quoteLines) {
        table.addCell(cell(line.getDescription(), body));
        table.addCell(cell(line.getQuantity().toPlainString(), body));
        table.addCell(cell(inr(line.getUnitPrice()), body));
        table.addCell(cell(inr(line.getLineTotal()), body));
      }
      document.add(table);
      document.add(new Paragraph(" "));
      document.add(new Paragraph("Subtotal  " + inr(quote.getSubtotal()), body));
      document.add(new Paragraph("Discount  " + inr(quote.getDiscountAmount()), body));
      document.add(new Paragraph("Tax " + quote.getTaxRate().toPlainString() + "%  " + inr(quote.getTaxAmount()), body));
      document.add(new Paragraph("Total  " + inr(quote.getTotalAmount()), label));
      if (quote.getNotes() != null && !quote.getNotes().isBlank()) {
        document.add(new Paragraph(" "));
        document.add(new Paragraph(quote.getNotes(), body));
      }
      document.close();
      return output.toByteArray();
    } catch (DocumentException exception) {
      throw new IllegalStateException("Could not render the quotation", exception);
    }
  }

  private static PdfPCell cell(String text, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setPadding(6);
    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
    return cell;
  }

  private static String address(Company company) {
    return String.join(", ", java.util.stream.Stream.of(company.getAddressLine(), company.getCity(), company.getState(), company.getPincode())
        .filter(part -> part != null && !part.isBlank())
        .toList());
  }

  private static String inr(BigDecimal value) {
    return "INR " + value.toPlainString();
  }

  private Quotation require(UUID id) {
    return quotations.findByIdAndCompanyId(id, companyId()).orElseThrow(NotFoundException::new);
  }

  private UUID companyId() {
    return currentUser.require().companyId();
  }

  public record LineInput(
      UUID productId,
      @NotBlank @Size(max = 240) String description,
      @NotNull BigDecimal quantity,
      @NotNull BigDecimal unitPrice
  ) {
  }

  public record QuoteInput(
      UUID leadId,
      UUID customerId,
      UUID surveyId,
      BigDecimal systemCapacityKw,
      Integer panelCount,
      BigDecimal discountAmount,
      BigDecimal taxRate,
      LocalDate validUntil,
      @Size(max = 2000) String notes,
      @NotEmpty List<@Valid LineInput> lines
  ) {
  }

  public record LineView(UUID id, UUID productId, String description, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    static LineView from(QuotationLine line) {
      return new LineView(line.getId(), line.getProductId(), line.getDescription(), line.getQuantity(), line.getUnitPrice(), line.getLineTotal());
    }
  }

  public record QuotationView(
      UUID id,
      String number,
      UUID leadId,
      UUID customerId,
      UUID surveyId,
      Quotation.Status status,
      BigDecimal systemCapacityKw,
      Integer panelCount,
      BigDecimal subtotal,
      BigDecimal discountAmount,
      BigDecimal taxRate,
      BigDecimal taxAmount,
      BigDecimal totalAmount,
      LocalDate validUntil,
      String notes,
      List<LineView> lines
  ) {
    static QuotationView from(Quotation quote, List<QuotationLine> quoteLines) {
      return new QuotationView(
          quote.getId(), quote.getNumber(), quote.getLeadId(), quote.getCustomerId(), quote.getSurveyId(), quote.getStatus(),
          quote.getSystemCapacityKw(), quote.getPanelCount(), quote.getSubtotal(), quote.getDiscountAmount(), quote.getTaxRate(),
          quote.getTaxAmount(), quote.getTotalAmount(), quote.getValidUntil(), quote.getNotes(),
          quoteLines.stream().map(LineView::from).toList()
      );
    }
  }
}
