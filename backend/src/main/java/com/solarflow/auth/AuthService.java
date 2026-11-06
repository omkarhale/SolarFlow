package com.solarflow.auth;

import com.solarflow.auth.domain.RefreshToken;
import com.solarflow.auth.domain.RefreshTokenRepository;
import com.solarflow.common.exception.BadRequestException;
import com.solarflow.common.exception.UnauthorizedException;
import com.solarflow.common.security.AuthUser;
import com.solarflow.common.security.CurrentUser;
import com.solarflow.user.domain.UserAccount;
import com.solarflow.user.domain.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  static final String COOKIE = "solarflow_refresh";

  private final UserAccountRepository users;
  private final RefreshTokenRepository tokens;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final CurrentUser currentUser;
  private final Duration refreshTtl;
  private final boolean cookieSecure;

  public AuthService(
      UserAccountRepository users,
      RefreshTokenRepository tokens,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      CurrentUser currentUser,
      @Value("${solarflow.auth.refresh-token-days}") long refreshDays,
      @Value("${solarflow.auth.cookie-secure}") boolean cookieSecure
  ) {
    this.users = users;
    this.tokens = tokens;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.currentUser = currentUser;
    this.refreshTtl = Duration.ofDays(refreshDays);
    this.cookieSecure = cookieSecure;
  }

  @Transactional
  public AuthResponse login(LoginRequest request, HttpServletRequest http, HttpServletResponse response) {
    UserAccount user = users.findByEmailIgnoreCase(request.email().trim().toLowerCase())
        .filter(account -> account.getStatus() == UserAccount.Status.ACTIVE)
        .filter(account -> passwordEncoder.matches(request.password(), account.getPasswordHash()))
        .orElseThrow(() -> new UnauthorizedException("Email or password is incorrect"));
    String raw = persistToken(user, UUID.randomUUID(), http);
    writeCookie(response, raw, refreshTtl);
    return AuthResponse.of(jwtService.issue(user), jwtService.accessSeconds(), user);
  }

  @Transactional(noRollbackFor = UnauthorizedException.class)
  public AuthResponse refresh(String rawToken, HttpServletRequest http, HttpServletResponse response) {
    if (rawToken == null || rawToken.isBlank()) {
      throw new UnauthorizedException("Sign in required");
    }
    RefreshToken existing = tokens.findByTokenHash(TokenHasher.sha256(rawToken))
        .orElseThrow(() -> new UnauthorizedException("Sign in required"));
    if (existing.getRevokedAt() != null) {
      revokeFamily(existing.getFamilyId());
      clearCookie(response);
      throw new UnauthorizedException("Sign in required");
    }
    if (existing.getExpiresAt().isBefore(Instant.now())) {
      clearCookie(response);
      throw new UnauthorizedException("Sign in required");
    }
    UserAccount user = users.findById(existing.getUserId())
        .filter(account -> account.getStatus() == UserAccount.Status.ACTIVE)
        .orElseThrow(() -> new UnauthorizedException("Sign in required"));
    existing.setRevokedAt(Instant.now());
    String raw = persistToken(user, existing.getFamilyId(), http);
    RefreshToken replacement = tokens.findByTokenHash(TokenHasher.sha256(raw)).orElseThrow();
    existing.setReplacedById(replacement.getId());
    writeCookie(response, raw, refreshTtl);
    return AuthResponse.of(jwtService.issue(user), jwtService.accessSeconds(), user);
  }

  @Transactional
  public void logout(String rawToken, HttpServletResponse response) {
    if (rawToken != null && !rawToken.isBlank()) {
      tokens.findByTokenHash(TokenHasher.sha256(rawToken)).ifPresent(token -> {
        if (token.getRevokedAt() == null) {
          token.setRevokedAt(Instant.now());
        }
      });
    }
    clearCookie(response);
  }

  @Transactional
  public void logoutAll(HttpServletResponse response) {
    AuthUser actor = currentUser.require();
    Instant now = Instant.now();
    tokens.findByUserId(actor.id()).forEach(token -> {
      if (token.getRevokedAt() == null) {
        token.setRevokedAt(now);
      }
    });
    clearCookie(response);
  }

  @Transactional
  public UserResponse changePassword(ChangePasswordRequest request) {
    AuthUser actor = currentUser.require();
    UserAccount user = users.findById(actor.id()).orElseThrow(() -> new UnauthorizedException("Sign in required"));
    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
      throw new BadRequestException("Current password is incorrect");
    }
    if (request.currentPassword().equals(request.newPassword())) {
      throw new BadRequestException("Choose a different password");
    }
    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    Instant now = Instant.now();
    tokens.findByUserId(user.getId()).forEach(token -> {
      if (token.getRevokedAt() == null) {
        token.setRevokedAt(now);
      }
    });
    return UserResponse.from(user);
  }

  public UserResponse me() {
    AuthUser actor = currentUser.require();
    return users.findById(actor.id()).map(UserResponse::from)
        .orElseThrow(() -> new UnauthorizedException("Sign in required"));
  }

  private String persistToken(UserAccount user, UUID familyId, HttpServletRequest http) {
    String raw = TokenHasher.newSecret();
    RefreshToken token = new RefreshToken();
    token.setUserId(user.getId());
    token.setCompanyId(user.getCompanyId());
    token.setFamilyId(familyId);
    token.setTokenHash(TokenHasher.sha256(raw));
    token.setExpiresAt(Instant.now().plus(refreshTtl));
    token.setUserAgent(trim(http.getHeader(HttpHeaders.USER_AGENT), 255));
    token.setIpAddress(trim(http.getRemoteAddr(), 64));
    tokens.saveAndFlush(token);
    return raw;
  }

  private void revokeFamily(UUID familyId) {
    Instant now = Instant.now();
    tokens.findByFamilyId(familyId).forEach(token -> {
      if (token.getRevokedAt() == null) {
        token.setRevokedAt(now);
      }
    });
  }

  private void writeCookie(HttpServletResponse response, String raw, Duration maxAge) {
    response.addHeader(HttpHeaders.SET_COOKIE, cookie(raw, maxAge).toString());
  }

  private void clearCookie(HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
  }

  private ResponseCookie cookie(String value, Duration maxAge) {
    return ResponseCookie.from(COOKIE, value)
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Lax")
        .path("/api")
        .maxAge(maxAge)
        .build();
  }

  private static String trim(String value, int max) {
    if (value == null) {
      return null;
    }
    return value.length() <= max ? value : value.substring(0, max);
  }
}
