package com.solarflow.project.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

  List<Project> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

  Optional<Project> findByIdAndCompanyId(UUID id, UUID companyId);

  List<Project> findByCompanyIdAndCustomerIdOrderByCreatedAtDesc(UUID companyId, UUID customerId);

  List<Project> findByCompanyIdAndStatusInOrderByTargetDateAsc(UUID companyId, List<Project.Status> statuses);
}
