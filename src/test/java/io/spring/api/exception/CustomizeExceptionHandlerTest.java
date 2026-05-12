package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
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

  private CustomizeExceptionHandler handler;
  private WebRequest webRequest;

  @BeforeEach
  void setUp() {
    handler = new CustomizeExceptionHandler();
    webRequest = new ServletWebRequest(new MockHttpServletRequest());
  }

  @Test
  void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "testObject");
    errors.addError(new FieldError("testObject", "title", "can't be empty"));
    InvalidRequestException exception = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  void should_handle_invalid_request_with_multiple_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "testObject");
    errors.addError(new FieldError("testObject", "title", "can't be empty"));
    errors.addError(new FieldError("testObject", "body", "can't be empty"));
    InvalidRequestException exception = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, webRequest);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    ErrorResource body = (ErrorResource) response.getBody();
    assertNotNull(body);
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, webRequest);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = Mockito.mock(ConstraintViolation.class);
    Mockito.doReturn(String.class).when(violation).getRootBeanClass();
    Path path = Mockito.mock(Path.class);
    Mockito.when(path.toString()).thenReturn("method.param.field");
    Mockito.when(violation.getPropertyPath()).thenReturn(path);
    Mockito.when(violation.getMessage()).thenReturn("must not be blank");
    ConstraintDescriptor descriptor = Mockito.mock(ConstraintDescriptor.class);
    java.lang.annotation.Annotation annotation =
        new javax.validation.constraints.NotBlank() {
          @Override
          public String message() {
            return "";
          }

          @Override
          public Class<?>[] groups() {
            return new Class[0];
          }

          @Override
          @SuppressWarnings("unchecked")
          public Class<? extends javax.validation.Payload>[] payload() {
            return new Class[0];
          }

          @Override
          public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return javax.validation.constraints.NotBlank.class;
          }
        };
    Mockito.doReturn(annotation).when(descriptor).getAnnotation();
    Mockito.doReturn(descriptor).when(violation).getConstraintDescriptor();
    violations.add(violation);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(ex, webRequest);

    assertNotNull(result);
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  void should_handle_constraint_violation_with_simple_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = Mockito.mock(ConstraintViolation.class);
    Mockito.doReturn(String.class).when(violation).getRootBeanClass();
    Path path = Mockito.mock(Path.class);
    Mockito.when(path.toString()).thenReturn("email");
    Mockito.when(violation.getPropertyPath()).thenReturn(path);
    Mockito.when(violation.getMessage()).thenReturn("invalid");
    ConstraintDescriptor descriptor = Mockito.mock(ConstraintDescriptor.class);
    java.lang.annotation.Annotation annotation =
        new javax.validation.constraints.NotBlank() {
          @Override
          public String message() {
            return "";
          }

          @Override
          public Class<?>[] groups() {
            return new Class[0];
          }

          @Override
          @SuppressWarnings("unchecked")
          public Class<? extends javax.validation.Payload>[] payload() {
            return new Class[0];
          }

          @Override
          public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return javax.validation.constraints.NotBlank.class;
          }
        };
    Mockito.doReturn(annotation).when(descriptor).getAnnotation();
    Mockito.doReturn(descriptor).when(violation).getConstraintDescriptor();
    violations.add(violation);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    ErrorResource result = handler.handleConstraintViolation(ex, webRequest);

    assertNotNull(result);
  }
}
