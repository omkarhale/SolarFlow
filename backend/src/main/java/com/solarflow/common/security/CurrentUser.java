package com.solarflow.common.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

  public AuthUser require() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser user)) {
      throw new IllegalStateException("Authenticated user is required");
    }
    return user;
  }
}
