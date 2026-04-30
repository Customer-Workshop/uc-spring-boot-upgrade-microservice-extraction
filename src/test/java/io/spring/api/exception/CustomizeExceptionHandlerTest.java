package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;
  @Mock private WebRequest webRequest;

  @BeforeEach
  void setUp() {
    handler = new CustomizeExceptionHandler();
  }

  @Test
  void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "article");
    bindingResult.addError(new FieldError("article", "title", "can't be empty"));

    InvalidRequestException exception = new InvalidRequestException(bindingResult);

    ResponseEntity<Object> result = handler.handleInvalidRequest(exception, webRequest);

    assertNotNull(result);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getStatusCode());
    assertNotNull(result.getBody());
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();

    ResponseEntity<Object> result = handler.handleInvalidAuthentication(exception, webRequest);

    assertNotNull(result);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getStatusCode());
  }

  @Test
  void should_handle_method_argument_not_valid() throws Exception {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "article");
    bindingResult.addError(new FieldError("article", "title", "can't be empty"));

    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(null, bindingResult);

    ResponseEntity<Object> result =
        handler.handleMethodArgumentNotValid(
            exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);

    assertNotNull(result);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getStatusCode());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_constraint_violation_exception() {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createArticle.param.title");
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public String message() {
                return "can't be empty";
              }

              @Override
              public Class<?>[] groups() {
                return new Class[0];
              }

              @Override
              public Class<? extends javax.validation.Payload>[] payload() {
                return new Class[0];
              }

              @Override
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }
            });
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("can't be empty");

    Set violations = new HashSet();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(cve, webRequest);

    assertNotNull(result);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_constraint_violation_with_simple_path() {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("simplefield");
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public String message() {
                return "required";
              }

              @Override
              public Class<?>[] groups() {
                return new Class[0];
              }

              @Override
              public Class<? extends javax.validation.Payload>[] payload() {
                return new Class[0];
              }

              @Override
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }
            });
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("required");

    Set violations = new HashSet();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(cve, webRequest);

    assertNotNull(result);
  }

  @Test
  void should_create_invalid_request_exception_with_errors() {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "article");
    bindingResult.addError(new FieldError("article", "title", "can't be empty"));

    InvalidRequestException exception = new InvalidRequestException(bindingResult);

    assertNotNull(exception.getErrors());
    assertEquals(1, exception.getErrors().getFieldErrorCount());
  }

  @Test
  void should_create_field_error_resource() {
    FieldErrorResource resource = new FieldErrorResource("resource", "field", "code", "message");

    assertEquals("resource", resource.getResource());
    assertEquals("field", resource.getField());
    assertEquals("code", resource.getCode());
    assertEquals("message", resource.getMessage());
  }
}
