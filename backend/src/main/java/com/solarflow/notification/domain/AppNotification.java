package com.solarflow.notification.domain;

import com.solarflow.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class AppNotification extends TenantEntity {

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false, length = 160)
  private String title;

  @Column(nullable = false, length = 500)
  private String body;

  @Column(nullable = false, length = 32)
  private String category;

  private Instant readAt;
}
