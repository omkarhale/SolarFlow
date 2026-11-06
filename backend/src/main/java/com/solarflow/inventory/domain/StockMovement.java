package com.solarflow.inventory.domain;

import com.solarflow.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
public class StockMovement extends TenantEntity {

  public enum Type {
    IN, OUT, ADJUSTMENT, ALLOCATION
  }

  @Column(nullable = false)
  private UUID productId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Type movementType;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal quantity;

  @Column(length = 32)
  private String referenceType;

  private UUID referenceId;

  @Column(length = 500)
  private String notes;

  private UUID createdBy;
}
