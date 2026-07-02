package io.spring.api.exception;

/** Thrown when a login attempt fails due to invalid email or password. Returns HTTP 422. */
public class InvalidAuthenticationException extends RuntimeException {

  public InvalidAuthenticationException() {
    super("invalid email or password");
  }
}
