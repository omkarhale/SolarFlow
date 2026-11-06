package com.solarflow.auth;

import com.solarflow.user.domain.UserAccount;
import java.util.UUID;

public record UserResponse(
    UUID id,
    UUID companyId,
    String email,
    String firstName,
    String lastName,
    String phone,
    UserAccount.Role role,
    UserAccount.Status status
) {

  public static UserResponse from(UserAccount user) {
    return new UserResponse(
        user.getId(),
        user.getCompanyId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getPhone(),
        user.getRole(),
        user.getStatus()
    );
  }
}
