package com.solarflow.project.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectPhotoRepository extends JpaRepository<ProjectPhoto, UUID> {

  List<ProjectPhoto> findByProjectIdAndCompanyIdOrderByCreatedAtDesc(UUID projectId, UUID companyId);

  Optional<ProjectPhoto> findByIdAndCompanyId(UUID id, UUID companyId);
}
