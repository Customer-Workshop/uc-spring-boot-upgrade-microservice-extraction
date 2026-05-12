package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;
  private WebRequest webRequest;

  @BeforeEach
  void setUp() {
    handler = new CustomizeExceptionHandler();
    webRequest = mock(WebRequest.class);
  }

  @Test
  public void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult("target", "target");
    errors.addError(new FieldError("target", "field1", "must not be null"));
    InvalidRequestException ex = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(ex, webRequest);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  public void should_handle_invalid_request_with_multiple_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult("target", "target");
    errors.addError(new FieldError("target", "email", "invalid email"));
    errors.addError(new FieldError("target", "username", "already taken"));
    InvalidRequestException ex = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(ex, webRequest);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(ex, webRequest);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("root.param.email", "invalid"));
    violations.add(createMockViolation("simpleName", "too short"));

    ConstraintViolationException ex = new ConstraintViolationException("validation", violations);

    ErrorResource result = handler.handleConstraintViolation(ex, webRequest);
    assertNotNull(result);
    assertFalse(result.getFieldErrors().isEmpty());
  }

  @Test
  public void should_handle_constraint_violation_with_simple_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("name", "required"));

    ConstraintViolationException ex = new ConstraintViolationException("validation", violations);

    ErrorResource result = handler.handleConstraintViolation(ex, webRequest);
    assertNotNull(result);
    assertEquals(1, result.getFieldErrors().size());
    assertEquals("name", result.getFieldErrors().get(0).getField());
  }

  @SuppressWarnings("unchecked")
  private ConstraintViolation<?> createMockViolation(String propertyPath, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    return violation;
  }
}
