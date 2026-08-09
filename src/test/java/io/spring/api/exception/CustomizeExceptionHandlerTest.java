package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

class CustomizeExceptionHandlerTest {
  private final CustomizeExceptionHandler handler = new CustomizeExceptionHandler();
  private final WebRequest request = mock(WebRequest.class);

  @Test
  void handlesInvalidRequestWithFieldErrors() {
    BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "article");
    binding.addError(
        new FieldError(
            "article",
            "title",
            null,
            false,
            new String[] {"NotBlank"},
            null,
            "must not be blank"));
    InvalidRequestException exception = new InvalidRequestException(binding);
    var response = handler.handleInvalidRequest(exception, request);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void handlesAuthenticationAndConstraintViolations() {
    var auth = handler.handleInvalidAuthentication(new InvalidAuthenticationException(), request);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, auth.getStatusCode());
    ErrorResource errors = handler.handleConstraintViolation(new ConstraintViolationException(Collections.emptySet()), request);
    assertNotNull(errors);
    assertNotNull(new InvalidRequestException(mock(org.springframework.validation.Errors.class)).getErrors());
  }
}
