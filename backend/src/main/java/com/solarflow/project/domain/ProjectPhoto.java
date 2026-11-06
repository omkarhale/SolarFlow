package com.solarflow.project.domain;

import com.solarflow.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "project_photos")
@Getter
@Setter
public class ProjectPhoto extends TenantEntity {

  @Column(nullable = false)
  private UUID projectId;

  @Column(nullable = false, length = 255)
  private String fileName;

  @Column(nullable = false, length = 255)
  private String storageKey;

  @Column(length = 240)
  private String caption;
}
