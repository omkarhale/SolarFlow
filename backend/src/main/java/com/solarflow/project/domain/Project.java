package com.solarflow.project.domain;

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
@Table(name = "projects")
@Getter
@Setter
public class Project extends TenantEntity {

  public enum Status {
    PLANNED, IN_PROGRESS, COMPLETED, ON_HOLD, CANCELLED
  }

  @Column(nullable = false)
  private UUID customerId;

  private UUID quotationId;

  @Column(nullable = false, length = 160)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Status status;

  @Column(precision = 8, scale = 2)
  private BigDecimal capacityKw;

  @Column(length = 240)
  private String siteAddress;

  private LocalDate startDate;

  private LocalDate targetDate;

  private LocalDate completedDate;

  private UUID leadTechnicianId;

  @Column(nullable = false)
  private int progressPercent;
}
