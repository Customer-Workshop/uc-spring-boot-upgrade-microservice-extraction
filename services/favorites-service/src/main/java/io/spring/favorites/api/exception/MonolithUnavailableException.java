package io.spring.favorites.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class MonolithUnavailableException extends RuntimeException {

  public MonolithUnavailableException(String endpoint, Throwable cause) {
    super("monolith call failed: " + endpoint, cause);
  }

  public MonolithUnavailableException(String endpoint, String reason) {
    super("monolith call failed: " + endpoint + " (" + reason + ")");
  }
}
