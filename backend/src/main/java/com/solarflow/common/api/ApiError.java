package com.solarflow.common.api;

import java.time.Instant;
import java.util.Map;

public record ApiError(
    String code,
    String message,
    Instant timestamp,
    Map<String, String> fields
) {

  public static ApiError of(String code, String message) {
    return new ApiError(code, message, Instant.now(), null);
  }

  public static ApiError of(String code, String message, Map<String, String> fields) {
    return new ApiError(code, message, Instant.now(), fields);
  }
}
