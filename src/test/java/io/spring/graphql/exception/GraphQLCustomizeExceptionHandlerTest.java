package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import java.lang.annotation.Annotation;
import java.util.Collections;
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

  private DataFetcherExceptionHandlerParameters mockParams(Throwable exception) {
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());
    return params;
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = mockParams(ex);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createUser.param.email", "can't be empty"));

    ConstraintViolationException cve = new ConstraintViolationException("validation", violations);
    DataFetcherExceptionHandlerParameters params = mockParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_other_exception_with_default_handler() {
    RuntimeException ex = new RuntimeException("generic error");
    DataFetcherExceptionHandlerParameters params = mockParams(ex);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  public void should_get_errors_as_data_with_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createUser.param.email", "can't be empty"));

    ConstraintViolationException cve = new ConstraintViolationException("validation", violations);

    Error result = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(result);
    assertEquals("BAD_REQUEST", result.getMessage());
    assertFalse(result.getErrors().isEmpty());
    assertEquals("email", result.getErrors().get(0).getKey());
  }

  @Test
  public void should_get_errors_as_data_with_empty_violations() {
    ConstraintViolationException cve =
        new ConstraintViolationException("validation", new HashSet<>());

    Error result = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(result);
    assertEquals("BAD_REQUEST", result.getMessage());
    assertTrue(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_single_path_segment_in_get_param() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "can't be empty"));

    ConstraintViolationException cve = new ConstraintViolationException("validation", violations);

    Error result = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(result);
    assertEquals("email", result.getErrors().get(0).getKey());
  }

  @Test
  public void should_handle_multiple_violations_same_field() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createUser.param.email", "can't be empty"));
    violations.add(createMockViolation("createUser.param.email", "invalid format"));

    ConstraintViolationException cve = new ConstraintViolationException("validation", violations);

    Error result = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(result);
    assertEquals("BAD_REQUEST", result.getMessage());
  }
}
