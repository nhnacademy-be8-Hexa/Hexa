package com.nhnacademy.hello.exception;

public class KeyManagerException extends RuntimeException {
  public KeyManagerException(String message) {
    super(message);
  }

  public KeyManagerException(String message, Throwable cause) {
    super(message, cause);
  }
}