package com.solarflow.quotation.domain;

import com.solarflow.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "quotation_lines")
@Getter
@Setter
public class QuotationLine extends TenantEntity {

  @Column(nullable = false)
  private UUID quotationId;

  private UUID productId;

  @Column(nullable = false, length = 240)
  private String description;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal quantity;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal unitPrice;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal lineTotal;
}
