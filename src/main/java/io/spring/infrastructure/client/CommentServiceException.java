package io.spring.infrastructure.client;

/** Raised when the comments microservice cannot be reached or returns an unexpected response. */
public class CommentServiceException extends RuntimeException {
  public CommentServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
