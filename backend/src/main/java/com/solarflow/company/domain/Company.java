package com.solarflow.company.domain;

import com.solarflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "companies")
@Getter
@Setter
public class Company extends BaseEntity {

  @Column(nullable = false, length = 160)
  private String name;

  @Column(length = 200)
  private String legalName;

  @Column(nullable = false, unique = true, length = 80)
  private String slug;

  @Column(length = 15)
  private String gstin;

  @Column(length = 160)
  private String email;

  @Column(length = 20)
  private String phone;

  @Column(length = 240)
  private String addressLine;

  @Column(length = 80)
  private String city;

  @Column(length = 80)
  private String state;

  @Column(length = 10)
  private String pincode;
}
