package io.spring.user.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidAuthenticationException extends RuntimeException {
  public InvalidAuthenticationException() {
    super("Invalid email or password");
  }
}
