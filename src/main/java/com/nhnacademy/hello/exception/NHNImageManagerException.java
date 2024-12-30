package com.nhnacademy.hello.exception;

public class NHNImageManagerException extends RuntimeException {
  public NHNImageManagerException(String message) {
    super(message);
  }

  public NHNImageManagerException(String message, Throwable cause) {
    super(message, cause);
  }
}
