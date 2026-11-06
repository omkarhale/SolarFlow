package com.solarflow.survey.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSurveyRepository extends JpaRepository<SiteSurvey, UUID> {

  List<SiteSurvey> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

  Optional<SiteSurvey> findByIdAndCompanyId(UUID id, UUID companyId);
}
