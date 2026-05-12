package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
public class CustomizeExceptionHandlerTest {

  @Mock private WebRequest webRequest;

  private CustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new CustomizeExceptionHandler();
  }

  @Test
  void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "article");
    errors.addError(new FieldError("article", "title", "can't be empty"));
    InvalidRequestException exception = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);

    assertEquals(422, response.getStatusCodeValue());
    assertNotNull(response.getBody());
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, webRequest);

    assertEquals(422, response.getStatusCodeValue());
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
    when(violation.getMessage()).thenReturn("must not be blank");

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    java.lang.annotation.Annotation annotation = mock(java.lang.annotation.Annotation.class);
    when(annotation.annotationType())
        .thenReturn((Class) javax.validation.constraints.NotBlank.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(cve, webRequest);

    assertNotNull(result);
    assertEquals(1, result.getFieldErrors().size());
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_constraint_violation_with_single_segment_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("invalid");

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    java.lang.annotation.Annotation annotation = mock(java.lang.annotation.Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) javax.validation.constraints.Email.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(cve, webRequest);

    assertNotNull(result);
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }

  @Test
  void should_serialize_error_resource_with_single_field_error() throws JsonProcessingException {
    FieldErrorResource fieldError =
        new FieldErrorResource("Article", "title", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fieldError));

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("title"));
    assertTrue(json.contains("can't be empty"));
  }

  @Test
  void should_serialize_error_resource_with_multiple_field_errors() throws JsonProcessingException {
    FieldErrorResource error1 =
        new FieldErrorResource("User", "email", "Email", "should be an email");
    FieldErrorResource error2 =
        new FieldErrorResource("User", "username", "NotBlank", "can't be empty");
    FieldErrorResource error3 =
        new FieldErrorResource("User", "email", "Duplicated", "email already exists");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(error1, error2, error3));

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("email"));
    assertTrue(json.contains("username"));
  }

  @Test
  void should_serialize_error_resource_with_empty_errors() throws JsonProcessingException {
    ErrorResource errorResource = new ErrorResource(Arrays.asList());

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
  }

  @Test
  void should_create_field_error_resource() {
    FieldErrorResource fieldError =
        new FieldErrorResource("Article", "title", "NotBlank", "can't be empty");

    assertEquals("Article", fieldError.getResource());
    assertEquals("title", fieldError.getField());
    assertEquals("NotBlank", fieldError.getCode());
    assertEquals("can't be empty", fieldError.getMessage());
  }

  @Test
  void should_create_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    assertNotNull(exception);
    assertTrue(exception instanceof RuntimeException);
  }

  @Test
  void should_create_resource_not_found_exception() {
    ResourceNotFoundException exception = new ResourceNotFoundException();
    assertNotNull(exception);
    assertTrue(exception instanceof RuntimeException);
  }

  @Test
  void should_create_no_authorization_exception() {
    NoAuthorizationException exception = new NoAuthorizationException();
    assertNotNull(exception);
    assertTrue(exception instanceof RuntimeException);
  }

  @Test
  void should_create_invalid_request_exception() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "article");
    errors.addError(new FieldError("article", "title", "can't be empty"));
    InvalidRequestException exception = new InvalidRequestException(errors);

    assertNotNull(exception);
    assertNotNull(exception.getErrors());
  }
}
