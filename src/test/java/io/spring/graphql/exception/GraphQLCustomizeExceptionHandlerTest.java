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

public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(ex);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> buildMockViolation(String pathStr, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
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
    violations.add(buildMockViolation("method.param.field", "can't be empty"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(cve);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_delegate_to_default_handler_for_other_exceptions() {
    RuntimeException ex = new RuntimeException("some error");
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(ex);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
  }

  @Test
  public void should_get_errors_as_data() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(buildMockViolation("method.param.email", "email already exists"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    Error errorResult = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(errorResult);
    assertEquals("BAD_REQUEST", errorResult.getMessage());
    assertFalse(errorResult.getErrors().isEmpty());
    assertEquals("email", errorResult.getErrors().get(0).getKey());
  }

  @Test
  public void should_handle_single_segment_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(buildMockViolation("singleParam", "required"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    Error errorResult = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(errorResult);
    assertEquals("singleParam", errorResult.getErrors().get(0).getKey());
  }

  @Test
  public void should_handle_multiple_violations_same_field() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(buildMockViolation("method.param.email", "too short"));
    violations.add(buildMockViolation("method.param.email", "invalid format"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    Error errorResult = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(errorResult);
    assertEquals(1, errorResult.getErrors().size());
    assertEquals(2, errorResult.getErrors().get(0).getValue().size());
  }

  @Test
  public void should_create_authentication_exception() {
    AuthenticationException ex = new AuthenticationException();
    assertNotNull(ex);
    assertTrue(ex instanceof RuntimeException);
  }
}
