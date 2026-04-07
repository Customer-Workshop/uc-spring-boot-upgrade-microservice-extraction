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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
public class CustomizeExceptionHandlerTest {

  private CustomizeExceptionHandler handler;

  @Mock private WebRequest webRequest;

  @BeforeEach
  public void setUp() {
    handler = new CustomizeExceptionHandler();
  }

  @SuppressWarnings("unchecked")
  private ConstraintViolation<Object> createMockViolation(String pathStr, String message) {
    ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);

    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation.getPropertyPath()).thenReturn(path);
    when(path.toString()).thenReturn(pathStr);
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(violation.getMessage()).thenReturn(message);
    return violation;
  }

  @Test
  public void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "article");
    errors.addError(new FieldError("article", "title", "can't be empty"));

    InvalidRequestException ex = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(ex, webRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(ex, webRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createArticle.param.title", "can't be empty"));

    ConstraintViolationException ex = new ConstraintViolationException("validation", violations);

    ErrorResource result = handler.handleConstraintViolation(ex, webRequest);

    assertNotNull(result);
    assertFalse(result.getFieldErrors().isEmpty());
    assertEquals("title", result.getFieldErrors().get(0).getField());
  }

  @Test
  public void should_handle_constraint_violation_with_single_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "invalid email"));

    ConstraintViolationException ex = new ConstraintViolationException("validation", violations);

    ErrorResource result = handler.handleConstraintViolation(ex, webRequest);

    assertNotNull(result);
    assertEquals("email", result.getFieldErrors().get(0).getField());
  }

  @Test
  public void should_handle_invalid_request_with_multiple_field_errors() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "article");
    errors.addError(new FieldError("article", "title", "can't be empty"));
    errors.addError(new FieldError("article", "body", "can't be empty"));

    InvalidRequestException ex = new InvalidRequestException(errors);

    ResponseEntity<Object> response = handler.handleInvalidRequest(ex, webRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }
}
