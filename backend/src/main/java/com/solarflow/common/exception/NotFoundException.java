package com.solarflow.common.exception;

public class NotFoundException extends RuntimeException {

  public NotFoundException() {
    super("Resource not found");
  }
}
