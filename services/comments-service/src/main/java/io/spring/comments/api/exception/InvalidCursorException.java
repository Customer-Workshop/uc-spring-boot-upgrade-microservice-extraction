package io.spring.comments.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCursorException extends RuntimeException {

  public InvalidCursorException(String cursor) {
    super("invalid cursor: " + cursor);
  }
}
