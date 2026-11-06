package com.solarflow.survey.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyPhotoRepository extends JpaRepository<SurveyPhoto, UUID> {

  List<SurveyPhoto> findBySurveyIdAndCompanyIdOrderByCreatedAtDesc(UUID surveyId, UUID companyId);

  Optional<SurveyPhoto> findByIdAndCompanyId(UUID id, UUID companyId);
}
