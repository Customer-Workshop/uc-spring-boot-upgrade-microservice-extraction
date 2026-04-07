package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.ErrorItem;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
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
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_handle_constraint_violation_exception() {
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    when(violation.getMessage()).thenReturn("email already exists");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    when(params.getException()).thenReturn(cve);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_generic_exception_with_default_handler() {
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    RuntimeException exception = new RuntimeException("generic error");
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_get_errors_as_data_from_constraint_violation() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    when(violation.getMessage()).thenReturn("email already exists");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_get_errors_as_data_with_multiple_violations_same_field() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();

    ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
    when(violation1.getRootBeanClass()).thenReturn((Class) String.class);
    Path path1 = mock(Path.class);
    when(path1.toString()).thenReturn("createUser.param.email");
    when(violation1.getPropertyPath()).thenReturn(path1);
    ConstraintDescriptor<?> descriptor1 = mock(ConstraintDescriptor.class);
    Annotation annotation1 = mock(Annotation.class);
    when(annotation1.annotationType()).thenReturn((Class) Override.class);
    when(descriptor1.getAnnotation()).thenReturn(annotation1);
    doReturn(descriptor1).when(violation1).getConstraintDescriptor();
    when(violation1.getMessage()).thenReturn("email already exists");
    violations.add(violation1);

    ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
    when(violation2.getRootBeanClass()).thenReturn((Class) String.class);
    Path path2 = mock(Path.class);
    when(path2.toString()).thenReturn("createUser.param.email");
    when(violation2.getPropertyPath()).thenReturn(path2);
    ConstraintDescriptor<?> descriptor2 = mock(ConstraintDescriptor.class);
    Annotation annotation2 = mock(Annotation.class);
    when(annotation2.annotationType()).thenReturn((Class) Override.class);
    when(descriptor2.getAnnotation()).thenReturn(annotation2);
    doReturn(descriptor2).when(violation2).getConstraintDescriptor();
    when(violation2.getMessage()).thenReturn("email is invalid");
    violations.add(violation2);

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    List<ErrorItem> errorItems = error.getErrors();
    assertNotNull(errorItems);
    assertFalse(errorItems.isEmpty());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_get_errors_as_data_with_violations_on_different_fields() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();

    ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
    when(violation1.getRootBeanClass()).thenReturn((Class) String.class);
    Path path1 = mock(Path.class);
    when(path1.toString()).thenReturn("createUser.param.email");
    when(violation1.getPropertyPath()).thenReturn(path1);
    ConstraintDescriptor<?> descriptor1 = mock(ConstraintDescriptor.class);
    Annotation annotation1 = mock(Annotation.class);
    when(annotation1.annotationType()).thenReturn((Class) Override.class);
    when(descriptor1.getAnnotation()).thenReturn(annotation1);
    doReturn(descriptor1).when(violation1).getConstraintDescriptor();
    when(violation1.getMessage()).thenReturn("email already exists");
    violations.add(violation1);

    ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
    when(violation2.getRootBeanClass()).thenReturn((Class) String.class);
    Path path2 = mock(Path.class);
    when(path2.toString()).thenReturn("createUser.param.username");
    when(violation2.getPropertyPath()).thenReturn(path2);
    ConstraintDescriptor<?> descriptor2 = mock(ConstraintDescriptor.class);
    Annotation annotation2 = mock(Annotation.class);
    when(annotation2.annotationType()).thenReturn((Class) Override.class);
    when(descriptor2.getAnnotation()).thenReturn(annotation2);
    doReturn(descriptor2).when(violation2).getConstraintDescriptor();
    when(violation2.getMessage()).thenReturn("username already exists");
    violations.add(violation2);

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertTrue(error.getErrors().size() >= 2);
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_handle_single_segment_property_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    when(violation.getMessage()).thenReturn("must not be blank");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_handle_constraint_violation_with_single_segment_path_in_exception_handler() {
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    when(violation.getMessage()).thenReturn("must not be blank");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    when(params.getException()).thenReturn(cve);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }
}
