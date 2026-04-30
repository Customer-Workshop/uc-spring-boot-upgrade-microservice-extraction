package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
  void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "testObject");
    bindingResult.addError(new FieldError("testObject", "name", "must not be blank"));
    InvalidRequestException ex = new InvalidRequestException(bindingResult);

    ResponseEntity<Object> response = handler.handleInvalidRequest(ex, webRequest);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
    assertInstanceOf(ErrorResource.class, response.getBody());
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(ex, webRequest);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("service.method.email");
    when(violation.getPropertyPath()).thenReturn(path);
    javax.validation.constraints.NotBlank annotation =
        mock(javax.validation.constraints.NotBlank.class);
    when(annotation.annotationType()).thenReturn((Class) javax.validation.constraints.NotBlank.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("must not be blank");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(cve, webRequest);

    assertNotNull(result);
    assertFalse(result.getFieldErrors().isEmpty());
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_handle_constraint_violation_with_simple_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("fieldname");
    when(violation.getPropertyPath()).thenReturn(path);
    javax.validation.constraints.NotBlank annotation2 =
        mock(javax.validation.constraints.NotBlank.class);
    when(annotation2.annotationType()).thenReturn((Class) javax.validation.constraints.NotBlank.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation2);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("invalid");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(cve, webRequest);

    assertNotNull(result);
    assertEquals("fieldname", result.getFieldErrors().get(0).getField());
  }
}
