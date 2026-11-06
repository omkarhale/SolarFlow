package com.solarflow.auth;

import com.solarflow.common.security.AuthUser;
import com.solarflow.user.domain.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey key;
  private final long accessMinutes;

  public JwtService(
      @Value("${solarflow.auth.jwt-secret}") String secret,
      @Value("${solarflow.auth.access-token-minutes}") long accessMinutes
  ) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessMinutes = accessMinutes;
  }

  public String issue(UserAccount user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(user.getId().toString())
        .claim("companyId", user.getCompanyId().toString())
        .claim("email", user.getEmail())
        .claim("role", user.getRole().name())
        .claim("firstName", user.getFirstName())
        .claim("lastName", user.getLastName())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(accessMinutes * 60)))
        .signWith(key)
        .compact();
  }

  public AuthUser parse(String token) {
    Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    return new AuthUser(
        UUID.fromString(claims.getSubject()),
        UUID.fromString(claims.get("companyId", String.class)),
        claims.get("email", String.class),
        claims.get("firstName", String.class),
        claims.get("lastName", String.class),
        UserAccount.Role.valueOf(claims.get("role", String.class))
    );
  }

  public long accessSeconds() {
    return accessMinutes * 60;
  }
}
