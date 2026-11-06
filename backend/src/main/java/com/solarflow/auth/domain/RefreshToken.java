package com.solarflow.auth.domain;

import com.solarflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
public class RefreshToken extends BaseEntity {

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private UUID companyId;

  @Column(nullable = false)
  private UUID familyId;

  @Column(nullable = false, unique = true, length = 128)
  private String tokenHash;

  @Column(nullable = false)
  private Instant expiresAt;

  private Instant revokedAt;

  private UUID replacedById;

  @Column(length = 255)
  private String userAgent;

  @Column(length = 64)
  private String ipAddress;
}
