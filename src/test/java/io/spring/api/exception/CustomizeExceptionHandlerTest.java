package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;
  private WebRequest webRequest;

  @BeforeEach
  void setUp() {
    handler = new CustomizeExceptionHandler();
    webRequest = mock(WebRequest.class);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> createMockViolation(String pathStr, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation.getMessage()).thenReturn(message);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);

    Annotation annotation =
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
          public Class<? extends javax.validation.Payload>[] payload() {
            return new Class[0];
          }

          @Override
          public Class<? extends Annotation> annotationType() {
            return javax.validation.constraints.NotBlank.class;
          }
        };

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    return violation;
  }

  @Test
  void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "article");
    bindingResult.addError(new FieldError("article", "title", "can't be empty"));

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
    assertInstanceOf(Map.class, response.getBody());
  }

  @Test
  void should_handle_constraint_violation_exception() {
    ConstraintViolation<?> violation =
        createMockViolation("createArticle.param.title", "must not be blank");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    ErrorResource result = handler.handleConstraintViolation(cve, webRequest);

    assertNotNull(result);
  }

  @Test
  void should_handle_constraint_violation_with_simple_path() {
    ConstraintViolation<?> violation = createMockViolation("fieldName", "invalid");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    ErrorResource result = handler.handleConstraintViolation(cve, webRequest);

    assertNotNull(result);
  }

  @Test
  void should_handle_method_argument_not_valid() throws Exception {
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "user");
    bindingResult.addError(new FieldError("user", "email", "must be a valid email"));
    bindingResult.addError(new FieldError("user", "username", "can't be empty"));

    java.lang.reflect.Method method = this.getClass().getDeclaredMethod("setUp");
    org.springframework.core.MethodParameter methodParameter =
        new org.springframework.core.MethodParameter(method, -1);

    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(methodParameter, bindingResult);

    ResponseEntity<Object> response =
        handler.handleMethodArgumentNotValid(
            ex, new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, webRequest);

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    assertNotNull(response.getBody());
  }
}
