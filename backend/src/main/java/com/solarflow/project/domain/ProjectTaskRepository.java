package com.solarflow.project.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, UUID> {

  List<ProjectTask> findByProjectIdAndCompanyIdOrderBySortOrderAsc(UUID projectId, UUID companyId);

  Optional<ProjectTask> findByIdAndCompanyId(UUID id, UUID companyId);
}
