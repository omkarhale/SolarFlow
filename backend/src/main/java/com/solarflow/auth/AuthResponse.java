package com.solarflow.auth;

import com.solarflow.user.domain.UserAccount;

public record AuthResponse(String accessToken, long expiresIn, UserResponse user) {

  public static AuthResponse of(String accessToken, long expiresIn, UserAccount user) {
    return new AuthResponse(accessToken, expiresIn, UserResponse.from(user));
  }
}
