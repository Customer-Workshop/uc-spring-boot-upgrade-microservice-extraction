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

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertEquals(1, result.getErrors().size());
  }

  @SuppressWarnings("unchecked")
  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("can't be empty");

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    doReturn(Override.class).when(annotation).annotationType();
    doReturn(annotation).when(descriptor).getAnnotation();
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(cve);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertEquals(1, result.getErrors().size());
  }

  @Test
  void should_handle_generic_exception_with_default_handler() {
    RuntimeException exception = new RuntimeException("generic error");
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @SuppressWarnings("unchecked")
  @Test
  void should_get_errors_as_data_from_constraint_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("already exists");

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    doReturn(Override.class).when(annotation).annotationType();
    doReturn(annotation).when(descriptor).getAnnotation();
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertFalse(error.getErrors().isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  void should_get_errors_as_data_with_simple_property_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("can't be empty");

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    doReturn(Override.class).when(annotation).annotationType();
    doReturn(annotation).when(descriptor).getAnnotation();
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
  }

  @SuppressWarnings("unchecked")
  @Test
  void should_handle_constraint_violation_with_deeply_nested_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("method.param.field.subfield");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("invalid");

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    doReturn(Override.class).when(annotation).annotationType();
    doReturn(annotation).when(descriptor).getAnnotation();
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(cve);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertEquals(1, result.getErrors().size());
  }
}
