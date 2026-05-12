package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthenticationExceptionTest {

  @Test
  void should_create_authentication_exception() {
    AuthenticationException exception = new AuthenticationException();
    assertNotNull(exception);
    assertTrue(exception instanceof RuntimeException);
  }
}
