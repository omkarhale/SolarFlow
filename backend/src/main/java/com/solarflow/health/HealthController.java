package com.solarflow.health;

import java.time.Instant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

  private final JdbcTemplate jdbcTemplate;

  public HealthController(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @GetMapping
  public HealthResponse health() {
    Integer result = jdbcTemplate.queryForObject("select 1", Integer.class);
    String database = result != null && result == 1 ? "up" : "down";
    return new HealthResponse("ok", "solarflow", database, Instant.now());
  }
}
