package com.solarflow.warranty;

import com.solarflow.common.exception.BadRequestException;
import com.solarflow.common.exception.NotFoundException;
import com.solarflow.common.security.CurrentUser;
import com.solarflow.customer.domain.CustomerRepository;
import com.solarflow.warranty.domain.Warranty;
import com.solarflow.warranty.domain.WarrantyRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/warranties")
public class WarrantyController {

  private final WarrantyRepository warranties;
  private final CustomerRepository customers;
  private final CurrentUser currentUser;

  public WarrantyController(WarrantyRepository warranties, CustomerRepository customers, CurrentUser currentUser) {
    this.warranties = warranties;
    this.customers = customers;
    this.currentUser = currentUser;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public WarrantyPage list() {
    UUID companyId = companyId();
    LocalDate soon = LocalDate.now().plusDays(60);
    var all = warranties.findByCompanyIdOrderByEndDateAsc(companyId).stream().map(WarrantyView::from).toList();
    var expiring = warranties.findByCompanyIdAndEndDateLessThanEqualOrderByEndDateAsc(companyId, soon).stream()
        .filter(item -> !item.getEndDate().isBefore(LocalDate.now()) && item.getStatus() != Warranty.Status.VOID)
        .map(WarrantyView::from)
        .toList();
    return new WarrantyPage(all, expiring);
  }

  @PostMapping
  @Transactional
  public WarrantyView create(@Valid @RequestBody WarrantyInput input) {
    if (input.endDate().isBefore(input.startDate())) {
      throw new BadRequestException("End date cannot be before the start date");
    }
    if (customers.findByIdAndCompanyId(input.customerId(), companyId()).isEmpty()) {
      throw new NotFoundException();
    }
    Warranty warranty = new Warranty();
    warranty.setCompanyId(companyId());
    warranty.setCustomerId(input.customerId());
    warranty.setProjectId(input.projectId());
    warranty.setProductId(input.productId());
    warranty.setWarrantyType(input.warrantyType());
    warranty.setStartDate(input.startDate());
    warranty.setEndDate(input.endDate());
    warranty.setStatus(input.status());
    warranty.setCoverageNotes(input.coverageNotes());
    return WarrantyView.from(warranties.save(warranty));
  }

  private UUID companyId() {
    return currentUser.require().companyId();
  }

  public record WarrantyInput(
      @NotNull UUID customerId,
      UUID projectId,
      UUID productId,
      @NotNull Warranty.Type warrantyType,
      @NotNull LocalDate startDate,
      @NotNull LocalDate endDate,
      @NotNull Warranty.Status status,
      @Size(max = 2000) String coverageNotes
  ) {
  }

  public record WarrantyView(
      UUID id, UUID customerId, UUID projectId, UUID productId, Warranty.Type warrantyType,
      LocalDate startDate, LocalDate endDate, Warranty.Status status, String coverageNotes
  ) {
    static WarrantyView from(Warranty warranty) {
      return new WarrantyView(
          warranty.getId(), warranty.getCustomerId(), warranty.getProjectId(), warranty.getProductId(),
          warranty.getWarrantyType(), warranty.getStartDate(), warranty.getEndDate(), warranty.getStatus(), warranty.getCoverageNotes()
      );
    }
  }

  public record WarrantyPage(List<WarrantyView> items, List<WarrantyView> expiring) {
  }
}
