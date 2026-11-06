package com.solarflow.user.domain;

import com.solarflow.common.entity.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserAccount extends TenantEntity {

  public enum Role {
    OWNER, ADMIN, SALES, OPERATIONS, TECHNICIAN
  }

  public enum Status {
    ACTIVE, INVITED, DISABLED
  }

  @Column(nullable = false, unique = true, length = 160)
  private String email;

  @Column(nullable = false, length = 100)
  private String passwordHash;

  @Column(nullable = false, length = 80)
  private String firstName;

  @Column(nullable = false, length = 80)
  private String lastName;

  @Column(length = 20)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Status status;
}
