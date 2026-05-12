package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
public class CustomizeExceptionHandlerTest {

  @InjectMocks private CustomizeExceptionHandler handler;

  @Test
  public void should_handle_invalid_request_exception() {
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "object");
    errors.addError(new FieldError("object", "field", "must not be blank"));

    InvalidRequestException exception = new InvalidRequestException(errors);
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidRequest(exception, request);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    WebRequest request = mock(WebRequest.class);

    ResponseEntity<Object> response = handler.handleInvalidAuthentication(exception, request);

    assertNotNull(response);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> createViolation(String propertyPath, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    doReturn(Override.class).when(annotation).annotationType();
    when(descriptor.getAnnotation()).thenReturn(annotation);
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    when(violation.getMessage()).thenReturn(message);

    return violation;
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("createUser.param.email", "email already exists"));

    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);
    WebRequest request = mock(WebRequest.class);

    ErrorResource result = handler.handleConstraintViolation(cve, request);

    assertNotNull(result);
    assertFalse(result.getFieldErrors().isEmpty());
  }

  @Test
  public void should_handle_constraint_violation_with_single_segment_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("singlefield", "error message"));

    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);
    WebRequest request = mock(WebRequest.class);

    ErrorResource result = handler.handleConstraintViolation(cve, request);

    assertNotNull(result);
  }
}
