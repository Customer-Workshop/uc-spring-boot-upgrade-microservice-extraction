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
  public void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  private DataFetcherExceptionHandlerParameters mockParams(Throwable exception) {
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());
    return params;
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = mockParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(mockConstraintViolation("email", "must not be empty"));

    ConstraintViolationException exception = new ConstraintViolationException("error", violations);
    DataFetcherExceptionHandlerParameters params = mockParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_constraint_violation_with_nested_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(mockConstraintViolation("param.user.email", "must not be empty"));

    ConstraintViolationException exception = new ConstraintViolationException("error", violations);
    DataFetcherExceptionHandlerParameters params = mockParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
  }

  @Test
  public void should_delegate_to_default_handler_for_other_exceptions() {
    RuntimeException exception = new RuntimeException("generic error");
    DataFetcherExceptionHandlerParameters params = mockParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
  }

  @Test
  public void should_get_errors_as_data_from_constraint_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(mockConstraintViolation("email", "must not be empty"));
    violations.add(mockConstraintViolation("username", "already exists"));

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  public void should_get_errors_as_data_with_multiple_violations_same_field() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(mockConstraintViolation("email", "must not be empty"));
    violations.add(mockConstraintViolation("email", "must be valid"));

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
  }

  @Test
  public void should_handle_single_segment_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(mockConstraintViolation("email", "invalid"));

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> mockConstraintViolation(String propertyPath, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Override.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    return violation;
  }
}
