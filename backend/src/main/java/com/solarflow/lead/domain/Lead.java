package com.solarflow.lead.domain;

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
@Table(name = "leads")
@Getter
@Setter
public class Lead extends TenantEntity {

  public enum Status {
    NEW, CONTACTED, SURVEY_SCHEDULED, QUOTED, NEGOTIATION, WON, LOST
  }

  public enum Priority {
    LOW, MEDIUM, HIGH
  }

  private UUID customerId;

  @Column(nullable = false, length = 160)
  private String contactName;

  @Column(length = 20)
  private String phone;

  @Column(length = 160)
  private String email;

  @Column(length = 80)
  private String city;

  @Column(length = 80)
  private String source;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Status status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Priority priority;

  private UUID assignedTo;

  private LocalDate followUpOn;

  @Column(precision = 8, scale = 2)
  private BigDecimal estimatedCapacityKw;

  @Column(length = 2000)
  private String notes;
}
