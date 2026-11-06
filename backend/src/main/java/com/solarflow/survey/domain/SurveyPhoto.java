package com.solarflow.survey.domain;

import com.solarflow.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "survey_photos")
@Getter
@Setter
public class SurveyPhoto extends TenantEntity {

  @Column(nullable = false)
  private UUID surveyId;

  @Column(nullable = false, length = 255)
  private String fileName;

  @Column(nullable = false, length = 255)
  private String storageKey;

  @Column(nullable = false, length = 120)
  private String contentType;
}
