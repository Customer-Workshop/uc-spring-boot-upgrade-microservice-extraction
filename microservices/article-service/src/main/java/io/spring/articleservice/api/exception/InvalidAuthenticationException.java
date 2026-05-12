package io.spring.articleservice.api.exception;

public class InvalidAuthenticationException extends RuntimeException {

  public InvalidAuthenticationException() {
    super("invalid email or password");
  }
}
