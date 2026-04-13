package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
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

@ExtendWith(MockitoExtension.class)
public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  private DataFetcherExceptionHandlerParameters buildParams(Throwable exception) {
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.parse("/test"));
    return params;
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
    assertEquals(1, result.getErrors().size());
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = createMockViolation("email", "must be valid");
    violations.add(violation);

    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);
    DataFetcherExceptionHandlerParameters params = buildParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
    assertEquals(1, result.getErrors().size());
  }

  @Test
  void should_handle_constraint_violation_with_multiple_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "must be valid"));
    violations.add(createMockViolation("username", "must not be blank"));

    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);
    DataFetcherExceptionHandlerParameters params = buildParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_with_same_field_multiple_errors() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "must be valid"));
    violations.add(createMockViolation("email", "must not be blank"));

    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);
    DataFetcherExceptionHandlerParameters params = buildParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_delegate_other_exceptions_to_default_handler() {
    RuntimeException exception = new RuntimeException("unexpected error");
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data_from_constraint_violation() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "must be valid"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void should_get_errors_as_data_with_multiple_fields() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "must be valid"));
    violations.add(createMockViolation("username", "must not be blank"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals(2, error.getErrors().size());
  }

  @Test
  void should_get_errors_as_data_with_same_field_multiple_messages() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "must be valid"));
    violations.add(createMockViolation("email", "must not be blank"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals(1, error.getErrors().size());
    assertEquals(2, error.getErrors().get(0).getValue().size());
  }

  @Test
  void should_handle_single_segment_property_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
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

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  void should_handle_deep_nested_property_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("invalid");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  private ConstraintViolation<?> createMockViolation(String field, String message) {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("service.param." + field);
    when(violation.getPropertyPath()).thenReturn(path);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn(message);
    return violation;
  }
}
