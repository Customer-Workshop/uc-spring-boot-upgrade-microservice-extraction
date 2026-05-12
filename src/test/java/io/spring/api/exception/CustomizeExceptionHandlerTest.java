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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new CustomizeExceptionHandler();
  }

  @Test
  void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "target");
    errors.addError(new FieldError("target", "email", "email is invalid"));
    InvalidRequestException exception = new InvalidRequestException(errors);
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, request);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  void should_handle_invalid_request_with_multiple_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "target");
    errors.addError(new FieldError("target", "email", "email is invalid"));
    errors.addError(new FieldError("target", "username", "username is taken"));
    InvalidRequestException exception = new InvalidRequestException(errors);
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations =
        buildViolationSet("param.field.email", "must not be blank");

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    WebRequest request = mock(WebRequest.class);

    ErrorResource result = handler.handleConstraintViolation(cve, request);

    assertNotNull(result);
    assertFalse(result.getFieldErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_with_single_path_segment() {
    Set<ConstraintViolation<?>> violations = buildViolationSet("email", "invalid");

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    WebRequest request = mock(WebRequest.class);

    ErrorResource result = handler.handleConstraintViolation(cve, request);
    assertNotNull(result);
    assertEquals(1, result.getFieldErrors().size());
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }

  @Test
  void should_create_invalid_request_exception_and_access_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "target");
    errors.addError(new FieldError("target", "title", "can't be empty"));

    InvalidRequestException exception = new InvalidRequestException(errors);

    assertNotNull(exception.getErrors());
    assertEquals(1, exception.getErrors().getFieldErrorCount());
    assertEquals(
        "can't be empty", exception.getErrors().getFieldError("title").getDefaultMessage());
  }

  @Test
  void should_create_field_error_resource() {
    FieldErrorResource resource =
        new FieldErrorResource("User", "email", "NotBlank", "must not be blank");
    assertEquals("User", resource.getResource());
    assertEquals("email", resource.getField());
    assertEquals("NotBlank", resource.getCode());
    assertEquals("must not be blank", resource.getMessage());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Set<ConstraintViolation<?>> buildViolationSet(String pathStr, String message) {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation =
        new Override() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return Override.class;
          }
        };
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn(message);
    violations.add(violation);
    return violations;
  }
}
