package com.solarflow.customer;

import com.solarflow.common.api.PageRequests;
import com.solarflow.common.api.PageResponse;
import com.solarflow.common.exception.NotFoundException;
import com.solarflow.common.security.CurrentUser;
import com.solarflow.customer.domain.Customer;
import com.solarflow.customer.domain.CustomerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

  private final CustomerRepository customers;
  private final CurrentUser currentUser;

  public CustomerController(CustomerRepository customers, CurrentUser currentUser) {
    this.customers = customers;
    this.currentUser = currentUser;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public PageResponse<CustomerView> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Customer.Status status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    var result = customers.search(currentUser.require().companyId(), blankToNull(q), status, PageRequests.of(page, size));
    return new PageResponse<>(result.map(CustomerView::from).getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public CustomerView get(@PathVariable UUID id) {
    return CustomerView.from(require(id));
  }

  @PostMapping
  @Transactional
  public CustomerView create(@Valid @RequestBody CustomerInput input) {
    Customer customer = new Customer();
    customer.setCompanyId(currentUser.require().companyId());
    apply(customer, input);
    return CustomerView.from(customers.save(customer));
  }

  @PatchMapping("/{id}")
  @Transactional
  public CustomerView update(@PathVariable UUID id, @Valid @RequestBody CustomerInput input) {
    Customer customer = require(id);
    apply(customer, input);
    return CustomerView.from(customer);
  }

  private Customer require(UUID id) {
    return customers.findByIdAndCompanyId(id, currentUser.require().companyId()).orElseThrow(NotFoundException::new);
  }

  private static void apply(Customer customer, CustomerInput input) {
    customer.setDisplayName(input.displayName().trim());
    customer.setPhone(input.phone());
    customer.setEmail(input.email());
    customer.setAddressLine(input.addressLine());
    customer.setCity(input.city());
    customer.setState(input.state());
    customer.setPincode(input.pincode());
    customer.setStatus(input.status());
    customer.setSource(input.source());
    customer.setNotes(input.notes());
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  public record CustomerInput(
      @NotBlank @Size(max = 160) String displayName,
      @Size(max = 20) String phone,
      @Size(max = 160) String email,
      @Size(max = 240) String addressLine,
      @Size(max = 80) String city,
      @Size(max = 80) String state,
      @Size(max = 10) String pincode,
      @NotNull Customer.Status status,
      @Size(max = 80) String source,
      @Size(max = 2000) String notes
  ) {
  }

  public record CustomerView(
      UUID id,
      String displayName,
      String phone,
      String email,
      String addressLine,
      String city,
      String state,
      String pincode,
      Customer.Status status,
      String source,
      String notes
  ) {
    static CustomerView from(Customer customer) {
      return new CustomerView(
          customer.getId(),
          customer.getDisplayName(),
          customer.getPhone(),
          customer.getEmail(),
          customer.getAddressLine(),
          customer.getCity(),
          customer.getState(),
          customer.getPincode(),
          customer.getStatus(),
          customer.getSource(),
          customer.getNotes()
      );
    }
  }
}
