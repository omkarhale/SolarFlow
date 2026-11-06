package com.solarflow.project.domain;

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
@Table(name = "project_tasks")
@Getter
@Setter
public class ProjectTask extends TenantEntity {

  public enum Status {
    TODO, IN_PROGRESS, DONE
  }

  @Column(nullable = false)
  private UUID projectId;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Status status;

  private UUID assignedTo;

  private LocalDate dueOn;

  @Column(nullable = false)
  private int sortOrder;
}
