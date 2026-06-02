package io.spring.articleservice.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class InvalidAuthenticationException extends RuntimeException {
  public InvalidAuthenticationException() {
    super();
  }
}
