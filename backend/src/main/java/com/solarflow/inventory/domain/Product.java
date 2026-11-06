package com.solarflow.inventory.domain;

import com.solarflow.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product extends TenantEntity {

  public enum Category {
    PANEL, INVERTER, STRUCTURE, CABLE, ELECTRICAL, OTHER
  }

  @Column(nullable = false, length = 64)
  private String sku;

  @Column(nullable = false, length = 160)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Category category;

  @Column(length = 80)
  private String brand;

  @Column(length = 80)
  private String model;

  @Column(nullable = false, length = 24)
  private String unit;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal unitPrice;

  private Integer wattage;

  private Integer warrantyYears;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal stockQuantity;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal reorderLevel;

  @Column(nullable = false)
  private boolean active;
}
