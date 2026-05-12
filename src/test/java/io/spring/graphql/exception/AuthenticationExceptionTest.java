package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AuthenticationExceptionTest {

  @Test
  void should_create_authentication_exception() {
    AuthenticationException exception = new AuthenticationException();
    assertNotNull(exception);
    assertTrue(exception instanceof RuntimeException);
  }

  @Test
  void should_be_throwable() {
    assertThrows(
        AuthenticationException.class,
        () -> {
          throw new AuthenticationException();
        });
  }
}
