package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.ErrorItem;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  @Test
  void should_handle_InvalidAuthenticationException() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_ConstraintViolationException() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mockConstraintViolation("param.title", "can't be empty");
    violations.add(violation);

    ConstraintViolationException exception = new ConstraintViolationException(violations);
    DataFetcherExceptionHandlerParameters params = mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_delegate_unknown_exception_to_default_handler() {
    RuntimeException exception = new RuntimeException("unknown error");
    DataFetcherExceptionHandlerParameters params = mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data_from_constraint_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(mockConstraintViolation("param.title", "can't be empty"));
    violations.add(mockConstraintViolation("param.body", "can't be empty"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void should_get_errors_as_data_groups_same_field_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(mockConstraintViolation("param.title", "can't be empty"));
    violations.add(mockConstraintViolation("param.title", "too short"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    // Both violations should be grouped under the same field
    for (ErrorItem item : error.getErrors()) {
      if ("title".equals(item.getKey())) {
        assertEquals(2, item.getValue().size());
      }
    }
  }

  @Test
  void should_handle_single_segment_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(mockConstraintViolation("title", "can't be empty"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    // Single segment path should be returned as-is
    boolean found = false;
    for (ErrorItem item : error.getErrors()) {
      if ("title".equals(item.getKey())) {
        found = true;
      }
    }
    assertTrue(found);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> mockConstraintViolation(String propertyPath, String message) {
    ConstraintViolation violation = Mockito.mock(ConstraintViolation.class);
    Path path = Mockito.mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn(String.class);

    ConstraintDescriptor descriptor = Mockito.mock(ConstraintDescriptor.class);
    Annotation annotation = Mockito.mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    return violation;
  }
}
