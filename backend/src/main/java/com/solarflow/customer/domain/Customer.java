package com.solarflow.customer.domain;

import com.solarflow.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter
@Setter
public class Customer extends TenantEntity {

  public enum Status {
    ACTIVE, INACTIVE
  }

  @Column(nullable = false, length = 160)
  private String displayName;

  @Column(length = 20)
  private String phone;

  @Column(length = 160)
  private String email;

  @Column(length = 240)
  private String addressLine;

  @Column(length = 80)
  private String city;

  @Column(length = 80)
  private String state;

  @Column(length = 10)
  private String pincode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Status status;

  @Column(length = 80)
  private String source;

  @Column(length = 2000)
  private String notes;
}
