package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class ExceptionClassesTest {

  @Test
  public void should_create_invalid_request_exception() {
    Errors errors = new BeanPropertyBindingResult(new Object(), "target");
    errors.rejectValue(null, "invalid");
    InvalidRequestException ex = new InvalidRequestException(errors);
    assertNotNull(ex.getErrors());
    assertTrue(ex.getErrors().hasErrors());
  }

  @Test
  public void should_create_resource_not_found_exception() {
    ResourceNotFoundException ex = new ResourceNotFoundException();
    assertNotNull(ex);
    assertTrue(ex instanceof RuntimeException);
  }

  @Test
  public void should_create_no_authorization_exception() {
    NoAuthorizationException ex = new NoAuthorizationException();
    assertNotNull(ex);
    assertTrue(ex instanceof RuntimeException);
  }

  @Test
  public void should_create_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    assertEquals("invalid email or password", ex.getMessage());
  }

  @Test
  public void should_create_error_resource() {
    FieldErrorResource fer = new FieldErrorResource("resource", "field", "code", "message");
    assertEquals("resource", fer.getResource());
    assertEquals("field", fer.getField());
    assertEquals("code", fer.getCode());
    assertEquals("message", fer.getMessage());

    ErrorResource er = new ErrorResource(java.util.Arrays.asList(fer));
    assertEquals(1, er.getFieldErrors().size());
  }
}
