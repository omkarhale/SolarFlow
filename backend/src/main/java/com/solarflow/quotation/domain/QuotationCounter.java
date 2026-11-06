package com.solarflow.quotation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "quotation_counters")
@Getter
@Setter
public class QuotationCounter {

  @Id
  private UUID companyId;

  @Column(nullable = false)
  private long nextValue;
}
