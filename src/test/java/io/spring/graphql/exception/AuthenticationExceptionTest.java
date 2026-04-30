package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthenticationExceptionTest {

  @Test
  void should_be_runtime_exception() {
    AuthenticationException exception = new AuthenticationException();
    assertNotNull(exception);
    assertTrue(exception instanceof RuntimeException);
  }

  @Test
  void should_be_throwable() {
    assertThrows(AuthenticationException.class, () -> {
      throw new AuthenticationException();
    });
  }
}
