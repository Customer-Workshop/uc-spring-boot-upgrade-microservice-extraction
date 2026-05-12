package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AuthenticationExceptionTest {

  @Test
  void should_create_authentication_exception() {
    AuthenticationException exception = new AuthenticationException();
    assertNotNull(exception);
    assertInstanceOf(RuntimeException.class, exception);
  }
}
