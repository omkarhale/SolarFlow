package com.solarflow.payment.domain;

import com.solarflow.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment extends TenantEntity {

  public enum Method {
    CASH, UPI, BANK_TRANSFER, CHEQUE, CARD
  }

  public enum Status {
    PENDING, RECEIVED, FAILED, REFUNDED
  }

  @Column(nullable = false)
  private UUID customerId;

  private UUID projectId;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Method method;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Status status;

  @Column(length = 64)
  private String referenceNo;

  @Column(length = 40)
  private String receiptNumber;

  private Instant paidAt;

  @Column(length = 500)
  private String notes;
}
