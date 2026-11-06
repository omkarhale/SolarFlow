package com.solarflow.quotation.domain;

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
@Table(name = "quotations")
@Getter
@Setter
public class Quotation extends TenantEntity {

  public enum Status {
    DRAFT, SENT, ACCEPTED, REJECTED, EXPIRED
  }

  @Column(nullable = false, length = 32)
  private String number;

  private UUID leadId;

  private UUID customerId;

  private UUID surveyId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Status status;

  @Column(precision = 8, scale = 2)
  private BigDecimal systemCapacityKw;

  private Integer panelCount;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal subtotal;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal discountAmount;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal taxRate;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal taxAmount;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal totalAmount;

  private LocalDate validUntil;

  @Column(length = 2000)
  private String notes;

  private UUID createdBy;
}
