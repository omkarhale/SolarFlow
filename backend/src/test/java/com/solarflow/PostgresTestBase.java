package com.solarflow;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.sql.Connection;
import java.sql.Statement;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class PostgresTestBase {

  private static EmbeddedPostgres postgres;

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", PostgresTestBase::jdbcUrl);
    registry.add("spring.datasource.username", () -> "postgres");
    registry.add("spring.datasource.password", () -> "");
    registry.add("solarflow.demo-data", () -> "false");
  }

  private static synchronized String jdbcUrl() {
    try {
      if (postgres == null) {
        postgres = EmbeddedPostgres.builder().start();
        try (Connection connection = postgres.getPostgresDatabase().getConnection();
             Statement statement = connection.createStatement()) {
          statement.execute("CREATE DATABASE solarflow");
        }
      }
      return postgres.getJdbcUrl("postgres", "solarflow");
    } catch (Exception exception) {
      throw new IllegalStateException("Embedded PostgreSQL failed to start", exception);
    }
  }
}
