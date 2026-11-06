package com.solarflow.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public AuthResponse login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest http,
      HttpServletResponse response
  ) {
    return authService.login(request, http, response);
  }

  @PostMapping("/refresh")
  public AuthResponse refresh(HttpServletRequest http, HttpServletResponse response) {
    return authService.refresh(readCookie(http), http, response);
  }

  @PostMapping("/logout")
  public void logout(HttpServletRequest http, HttpServletResponse response) {
    authService.logout(readCookie(http), response);
  }

  @PostMapping("/logout-all")
  public void logoutAll(HttpServletResponse response) {
    authService.logoutAll(response);
  }

  @GetMapping("/me")
  public UserResponse me() {
    return authService.me();
  }

  @PostMapping("/change-password")
  public UserResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
    return authService.changePassword(request);
  }

  private static String readCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (AuthService.COOKIE.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
