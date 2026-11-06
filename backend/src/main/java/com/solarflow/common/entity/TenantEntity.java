package com.solarflow.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class TenantEntity extends BaseEntity {

  @Column(nullable = false, updatable = false)
  private UUID companyId;
}
