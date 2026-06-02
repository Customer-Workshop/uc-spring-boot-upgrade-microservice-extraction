package io.spring.articleservice.api.exception;

import org.springframework.validation.Errors;

public class InvalidRequestException extends RuntimeException {
  private final Errors errors;

  public InvalidRequestException(Errors errors) {
    super("");
    this.errors = errors;
  }

  public Errors getErrors() {
    return errors;
  }
}
