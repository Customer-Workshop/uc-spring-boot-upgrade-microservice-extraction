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
  private WebRequest webRequest;

  @BeforeEach
  void setUp() {
    handler = new CustomizeExceptionHandler();
    webRequest = mock(WebRequest.class);
  }

  @Test
  void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult("target", "article");
    errors.addError(new FieldError("article", "title", "can't be empty"));

    InvalidRequestException exception = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, webRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @SuppressWarnings("unchecked")
  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("already exists");

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    doReturn(Override.class).when(annotation).annotationType();
    doReturn(annotation).when(descriptor).getAnnotation();
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(cve, webRequest);

    assertNotNull(result);
  }

  @SuppressWarnings("unchecked")
  @Test
  void should_handle_constraint_violation_with_simple_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("invalid");

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    doReturn(Override.class).when(annotation).annotationType();
    doReturn(annotation).when(descriptor).getAnnotation();
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(cve, webRequest);

    assertNotNull(result);
  }

  @Test
  void should_create_invalid_request_exception_with_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult("target", "article");
    errors.addError(new FieldError("article", "title", "can't be empty"));

    InvalidRequestException exception = new InvalidRequestException(errors);

    assertNotNull(exception.getErrors());
    assertEquals(1, exception.getErrors().getFieldErrorCount());
  }

  @Test
  void should_create_field_error_resource() {
    FieldErrorResource resource =
        new FieldErrorResource("Article", "title", "NotBlank", "can't be empty");

    assertEquals("Article", resource.getResource());
    assertEquals("title", resource.getField());
    assertEquals("NotBlank", resource.getCode());
    assertEquals("can't be empty", resource.getMessage());
  }
}
