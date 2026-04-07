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
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation =
        createMockViolation("field.param.email", "email is invalid");
    violations.add(violation);

    ConstraintViolationException exception = new ConstraintViolationException("error", violations);

    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
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

  @Test
  void should_get_errors_as_data_from_constraint_violation() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation =
        createMockViolation("field.param.email", "email is invalid");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void should_get_errors_as_data_with_multiple_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("field.param.email", "email is invalid"));
    violations.add(createMockViolation("field.param.email", "email already exists"));
    violations.add(createMockViolation("field.param.username", "username is required"));

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertTrue(error.getErrors().size() >= 2);
  }

  @Test
  void should_handle_constraint_violation_with_single_path_segment() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = createMockViolation("email", "email is invalid");
    violations.add(violation);

    ConstraintViolationException exception = new ConstraintViolationException("error", violations);

    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_get_errors_as_data_with_single_path_segment() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "email is invalid"));

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
  }

  @Test
  void should_handle_constraint_violation_with_multiple_violations_same_field() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("field.param.email", "email is required"));
    violations.add(createMockViolation("field.param.email", "email is invalid"));

    ConstraintViolationException exception = new ConstraintViolationException("error", violations);

    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> createMockViolation(String propertyPath, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    javax.validation.Path path = mock(javax.validation.Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn(Object.class);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation =
        new Annotation() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return Override.class;
          }
        };
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    return violation;
  }
}
