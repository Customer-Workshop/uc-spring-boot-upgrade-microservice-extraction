package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

public class InvalidRequestExceptionTest {

  @Test
  public void should_create_invalid_request_exception_with_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "object");
    errors.addError(new FieldError("object", "title", "must not be blank"));

    InvalidRequestException exception = new InvalidRequestException(errors);

    assertNotNull(exception);
    assertNotNull(exception.getErrors());
    assertTrue(exception.getErrors().hasErrors());
    assertEquals(1, exception.getErrors().getFieldErrorCount());
  }

  @Test
  public void should_create_invalid_request_exception_with_no_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "object");

    InvalidRequestException exception = new InvalidRequestException(errors);

    assertNotNull(exception);
    assertFalse(exception.getErrors().hasErrors());
  }
}
