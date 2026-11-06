package com.solarflow.service;

import com.solarflow.common.exception.NotFoundException;
import com.solarflow.common.security.CurrentUser;
import com.solarflow.customer.domain.CustomerRepository;
import com.solarflow.service.domain.ServiceRequest;
import com.solarflow.service.domain.ServiceRequestRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
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
@RequestMapping("/api/v1/service-requests")
public class ServiceRequestController {

  private final ServiceRequestRepository requests;
  private final CustomerRepository customers;
  private final CurrentUser currentUser;

  public ServiceRequestController(ServiceRequestRepository requests, CustomerRepository customers, CurrentUser currentUser) {
    this.requests = requests;
    this.customers = customers;
    this.currentUser = currentUser;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public List<ServiceView> list() {
    return requests.findByCompanyIdOrderByCreatedAtDesc(companyId()).stream().map(ServiceView::from).toList();
  }

  @PostMapping
  @Transactional
  public ServiceView create(@Valid @RequestBody ServiceInput input) {
    if (customers.findByIdAndCompanyId(input.customerId(), companyId()).isEmpty()) {
      throw new NotFoundException();
    }
    ServiceRequest request = new ServiceRequest();
    request.setCompanyId(companyId());
    request.setOpenedAt(Instant.now());
    apply(request, input);
    return ServiceView.from(requests.save(request));
  }

  @PatchMapping("/{id}")
  @Transactional
  public ServiceView update(@PathVariable UUID id, @Valid @RequestBody ServiceInput input) {
    ServiceRequest request = requests.findByIdAndCompanyId(id, companyId()).orElseThrow(NotFoundException::new);
    apply(request, input);
    if (input.status() == ServiceRequest.Status.RESOLVED || input.status() == ServiceRequest.Status.CLOSED) {
      if (request.getResolvedAt() == null) {
        request.setResolvedAt(Instant.now());
      }
    }
    return ServiceView.from(request);
  }

  private void apply(ServiceRequest request, ServiceInput input) {
    request.setCustomerId(input.customerId());
    request.setProjectId(input.projectId());
    request.setWarrantyId(input.warrantyId());
    request.setTitle(input.title().trim());
    request.setDescription(input.description());
    request.setPriority(input.priority());
    request.setStatus(input.status());
    request.setAssignedTo(input.assignedTo());
    request.setResolutionNotes(input.resolutionNotes());
  }

  private UUID companyId() {
    return currentUser.require().companyId();
  }

  public record ServiceInput(
      @NotNull UUID customerId,
      UUID projectId,
      UUID warrantyId,
      @NotBlank @Size(max = 160) String title,
      @Size(max = 2000) String description,
      @NotNull ServiceRequest.Priority priority,
      @NotNull ServiceRequest.Status status,
      UUID assignedTo,
      @Size(max = 2000) String resolutionNotes
  ) {
  }

  public record ServiceView(
      UUID id, UUID customerId, UUID projectId, UUID warrantyId, String title, String description,
      ServiceRequest.Priority priority, ServiceRequest.Status status, UUID assignedTo, String resolutionNotes,
      Instant openedAt, Instant resolvedAt
  ) {
    static ServiceView from(ServiceRequest request) {
      return new ServiceView(
          request.getId(), request.getCustomerId(), request.getProjectId(), request.getWarrantyId(), request.getTitle(),
          request.getDescription(), request.getPriority(), request.getStatus(), request.getAssignedTo(),
          request.getResolutionNotes(), request.getOpenedAt(), request.getResolvedAt()
      );
    }
  }
}
