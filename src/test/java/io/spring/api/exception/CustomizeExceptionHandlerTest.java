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
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new CustomizeExceptionHandler();
  }

  @Test
  void should_handle_invalid_request_exception() {
    BindException bindException = new BindException(new Object(), "article");
    bindException.addError(new FieldError("article", "title", "can't be empty"));
    InvalidRequestException exception = new InvalidRequestException(bindException);
    WebRequest webRequest = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    WebRequest webRequest = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, webRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("title");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("can't be empty");
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);
    WebRequest webRequest = mock(WebRequest.class);

    ErrorResource result = handler.handleConstraintViolation(cve, webRequest);

    assertNotNull(result);
    assertFalse(result.getFieldErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_constraint_violation_with_dotted_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("must not be blank");
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);
    WebRequest webRequest = mock(WebRequest.class);

    ErrorResource result = handler.handleConstraintViolation(cve, webRequest);

    assertNotNull(result);
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }

  @Test
  void should_create_resource_not_found_exception() {
    ResourceNotFoundException exception = new ResourceNotFoundException();
    assertNotNull(exception);
    assertInstanceOf(RuntimeException.class, exception);
  }

  @Test
  void should_create_no_authorization_exception() {
    NoAuthorizationException exception = new NoAuthorizationException();
    assertNotNull(exception);
    assertInstanceOf(RuntimeException.class, exception);
  }

  @Test
  void should_create_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    assertNotNull(exception);
    assertInstanceOf(RuntimeException.class, exception);
  }

  @Test
  void should_create_invalid_request_exception_with_errors() {
    BindException bindException = new BindException(new Object(), "article");
    InvalidRequestException exception = new InvalidRequestException(bindException);

    assertNotNull(exception);
    assertEquals(bindException, exception.getErrors());
  }
}
