package com.solarflow.common.security;

import com.solarflow.user.domain.UserAccount;
import java.util.UUID;

public record AuthUser(
    UUID id,
    UUID companyId,
    String email,
    String firstName,
    String lastName,
    UserAccount.Role role
) {
}
