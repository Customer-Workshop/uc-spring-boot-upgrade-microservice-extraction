package io.spring.comments.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class MonolithUnavailableException extends RuntimeException {

  public MonolithUnavailableException(String endpoint) {
    super("monolith call failed: " + endpoint);
  }

  public MonolithUnavailableException(String endpoint, Throwable cause) {
    super("monolith call failed: " + endpoint, cause);
  }
}
