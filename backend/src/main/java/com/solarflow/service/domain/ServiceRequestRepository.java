package com.solarflow.service.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {

  List<ServiceRequest> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

  Optional<ServiceRequest> findByIdAndCompanyId(UUID id, UUID companyId);
}
