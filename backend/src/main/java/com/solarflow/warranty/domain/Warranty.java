package com.solarflow.warranty.domain;

import com.solarflow.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "warranties")
@Getter
@Setter
public class Warranty extends TenantEntity {

  public enum Type {
    PANEL, INVERTER, WORKMANSHIP, OTHER
  }

  public enum Status {
    ACTIVE, EXPIRING, EXPIRED, VOID
  }

  @Column(nullable = false)
  private UUID customerId;

  private UUID projectId;

  private UUID productId;

  @Enumerated(EnumType.STRING)
  @Column(name = "warranty_type", nullable = false, length = 32)
  private Type warrantyType;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Status status;

  @Column(length = 2000)
  private String coverageNotes;
}
