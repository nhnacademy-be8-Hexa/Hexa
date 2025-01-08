package com.nhnacademy.hello.exception.imagemanager;

public class NHNImageManagerException extends RuntimeException {

  public NHNImageManagerException(String message) {
    super(message);
  }

  public NHNImageManagerException(String message, Throwable cause) {
    super(message, cause);
  }
}

