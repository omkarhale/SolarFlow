package com.solarflow.service.domain;

import com.solarflow.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "service_requests")
@Getter
@Setter
public class ServiceRequest extends TenantEntity {

  public enum Priority {
    LOW, MEDIUM, HIGH, URGENT
  }

  public enum Status {
    OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
  }

  @Column(nullable = false)
  private UUID customerId;

  private UUID projectId;

  private UUID warrantyId;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Priority priority;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Status status;

  private UUID assignedTo;

  @Column(length = 2000)
  private String resolutionNotes;

  @Column(nullable = false)
  private Instant openedAt;

  private Instant resolvedAt;
}
