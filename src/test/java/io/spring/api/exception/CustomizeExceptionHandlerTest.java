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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
public class CustomizeExceptionHandlerTest {

  @InjectMocks private CustomizeExceptionHandler handler;

  @Test
  void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "testObject");
    bindingResult.addError(new FieldError("testObject", "title", "can't be empty"));
    InvalidRequestException exception = new InvalidRequestException(bindingResult);
    WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void should_handle_invalid_request_with_multiple_field_errors() {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "testObject");
    bindingResult.addError(new FieldError("testObject", "title", "can't be empty"));
    bindingResult.addError(new FieldError("testObject", "body", "can't be empty"));
    InvalidRequestException exception = new InvalidRequestException(bindingResult);
    WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, request);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("can't be empty");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

    ErrorResource result = handler.handleConstraintViolation(cve, request);

    assertNotNull(result);
    assertFalse(result.getFieldErrors().isEmpty());
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_constraint_violation_with_single_path_segment() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("invalid");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

    ErrorResource result = handler.handleConstraintViolation(cve, request);

    assertNotNull(result);
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }

  @Test
  void should_create_invalid_request_exception() {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "testObject");
    bindingResult.addError(new FieldError("testObject", "title", "can't be empty"));
    InvalidRequestException exception = new InvalidRequestException(bindingResult);

    assertNotNull(exception.getErrors());
    assertTrue(exception.getErrors().hasErrors());
  }

  @Test
  void should_create_field_error_resource() {
    FieldErrorResource fer = new FieldErrorResource("resource", "field", "code", "message");

    assertEquals("resource", fer.getResource());
    assertEquals("field", fer.getField());
    assertEquals("code", fer.getCode());
    assertEquals("message", fer.getMessage());
  }
}
