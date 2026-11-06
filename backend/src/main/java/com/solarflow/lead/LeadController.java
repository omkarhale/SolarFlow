package com.solarflow.lead;

import com.solarflow.common.exception.NotFoundException;
import com.solarflow.common.security.CurrentUser;
import com.solarflow.customer.domain.Customer;
import com.solarflow.customer.domain.CustomerRepository;
import com.solarflow.lead.domain.Lead;
import com.solarflow.lead.domain.LeadRepository;
import com.solarflow.user.domain.UserAccountRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {

  private final LeadRepository leads;
  private final CustomerRepository customers;
  private final UserAccountRepository users;
  private final CurrentUser currentUser;

  public LeadController(
      LeadRepository leads,
      CustomerRepository customers,
      UserAccountRepository users,
      CurrentUser currentUser
  ) {
    this.leads = leads;
    this.customers = customers;
    this.users = users;
    this.currentUser = currentUser;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public List<LeadView> list() {
    return leads.findByCompanyIdOrderByCreatedAtDesc(companyId()).stream().map(LeadView::from).toList();
  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public LeadView get(@PathVariable UUID id) {
    return LeadView.from(require(id));
  }

  @PostMapping
  @Transactional
  public LeadView create(@Valid @RequestBody LeadInput input) {
    Lead lead = new Lead();
    lead.setCompanyId(companyId());
    apply(lead, input);
    return LeadView.from(leads.save(lead));
  }

  @PatchMapping("/{id}")
  @Transactional
  public LeadView update(@PathVariable UUID id, @Valid @RequestBody LeadInput input) {
    Lead lead = require(id);
    apply(lead, input);
    return LeadView.from(lead);
  }

  @PostMapping("/{id}/convert")
  @Transactional
  public LeadView convert(@PathVariable UUID id) {
    Lead lead = require(id);
    if (lead.getCustomerId() == null) {
      Customer customer = new Customer();
      customer.setCompanyId(lead.getCompanyId());
      customer.setDisplayName(lead.getContactName());
      customer.setPhone(lead.getPhone());
      customer.setEmail(lead.getEmail());
      customer.setCity(lead.getCity());
      customer.setStatus(Customer.Status.ACTIVE);
      customer.setSource(lead.getSource());
      customers.save(customer);
      lead.setCustomerId(customer.getId());
    }
    lead.setStatus(Lead.Status.WON);
    return LeadView.from(lead);
  }

  private void apply(Lead lead, LeadInput input) {
    UUID companyId = lead.getCompanyId();
    if (input.customerId() != null && customers.findByIdAndCompanyId(input.customerId(), companyId).isEmpty()) {
      throw new NotFoundException();
    }
    if (input.assignedTo() != null && users.findById(input.assignedTo()).filter(user -> companyId.equals(user.getCompanyId())).isEmpty()) {
      throw new NotFoundException();
    }
    lead.setCustomerId(input.customerId());
    lead.setContactName(input.contactName().trim());
    lead.setPhone(input.phone());
    lead.setEmail(input.email());
    lead.setCity(input.city());
    lead.setSource(input.source());
    lead.setStatus(input.status());
    lead.setPriority(input.priority());
    lead.setAssignedTo(input.assignedTo());
    lead.setFollowUpOn(input.followUpOn());
    lead.setEstimatedCapacityKw(input.estimatedCapacityKw());
    lead.setNotes(input.notes());
  }

  private Lead require(UUID id) {
    return leads.findByIdAndCompanyId(id, companyId()).orElseThrow(NotFoundException::new);
  }

  private UUID companyId() {
    return currentUser.require().companyId();
  }

  public record LeadInput(
      UUID customerId,
      @NotBlank @Size(max = 160) String contactName,
      @Size(max = 20) String phone,
      @Size(max = 160) String email,
      @Size(max = 80) String city,
      @Size(max = 80) String source,
      @NotNull Lead.Status status,
      @NotNull Lead.Priority priority,
      UUID assignedTo,
      LocalDate followUpOn,
      BigDecimal estimatedCapacityKw,
      @Size(max = 2000) String notes
  ) {
  }

  public record LeadView(
      UUID id,
      UUID customerId,
      String contactName,
      String phone,
      String email,
      String city,
      String source,
      Lead.Status status,
      Lead.Priority priority,
      UUID assignedTo,
      LocalDate followUpOn,
      BigDecimal estimatedCapacityKw,
      String notes
  ) {
    static LeadView from(Lead lead) {
      return new LeadView(
          lead.getId(), lead.getCustomerId(), lead.getContactName(), lead.getPhone(), lead.getEmail(),
          lead.getCity(), lead.getSource(), lead.getStatus(), lead.getPriority(), lead.getAssignedTo(),
          lead.getFollowUpOn(), lead.getEstimatedCapacityKw(), lead.getNotes()
      );
    }
  }
}
