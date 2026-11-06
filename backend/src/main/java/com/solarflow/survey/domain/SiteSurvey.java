package com.solarflow.survey.domain;

import com.solarflow.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "site_surveys")
@Getter
@Setter
public class SiteSurvey extends TenantEntity {

  public enum Status {
    SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
  }

  @Column(nullable = false)
  private UUID leadId;

  private UUID customerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Status status;

  private LocalDate scheduledOn;

  private UUID surveyedBy;

  @Column(length = 80)
  private String roofType;

  @Column(precision = 10, scale = 2)
  private BigDecimal roofAreaSqft;

  @Column(length = 32)
  private String orientation;

  @Column(length = 500)
  private String shadingNotes;

  @Column(precision = 12, scale = 2)
  private BigDecimal monthlyBill;

  @Column(precision = 8, scale = 2)
  private BigDecimal sanctionedLoadKw;

  @Column(length = 32)
  private String connectionType;

  @Column(precision = 9, scale = 6)
  private BigDecimal latitude;

  @Column(precision = 9, scale = 6)
  private BigDecimal longitude;

  @Column(length = 2000)
  private String notes;
}
