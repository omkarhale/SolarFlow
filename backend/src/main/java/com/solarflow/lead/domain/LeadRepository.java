package com.solarflow.lead.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

  List<Lead> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

  Optional<Lead> findByIdAndCompanyId(UUID id, UUID companyId);

  long countByCompanyIdAndStatus(UUID companyId, Lead.Status status);

  long countByCompanyIdAndStatusNotIn(UUID companyId, List<Lead.Status> statuses);
}
