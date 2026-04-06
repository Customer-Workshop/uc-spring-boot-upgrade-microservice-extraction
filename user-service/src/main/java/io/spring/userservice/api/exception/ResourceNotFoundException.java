package io.spring.userservice.api.exception;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException() {
    super("resource not found");
  }
}
