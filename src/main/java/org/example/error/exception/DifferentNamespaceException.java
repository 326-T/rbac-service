package org.example.error.exception;

import lombok.Getter;

@Getter
public class DifferentNamespaceException extends RuntimeException {

  private final String detail;

  public DifferentNamespaceException(String message) {
    super(message);
    detail = "%s.%s".formatted(Thread.currentThread().getStackTrace()[2].getClassName(),
        Thread.currentThread().getStackTrace()[2].getMethodName());
  }

}
