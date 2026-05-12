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

  private DataFetcherExceptionHandlerParameters buildParams(Exception exception) {
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());
    return params;
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> buildViolation(String pathStr, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    return violation;
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(buildViolation("createUser.param.email", "must not be blank"));

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);
    DataFetcherExceptionHandlerParameters params = buildParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_delegate_to_default_handler_for_other_exceptions() {
    RuntimeException exception = new RuntimeException("general error");
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data_with_dotted_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(buildViolation("createUser.param.email", "must not be blank"));

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  void should_get_errors_as_data_with_simple_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(buildViolation("email", "invalid email"));

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  void should_get_errors_as_data_with_multiple_violations_same_field() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(buildViolation("createUser.param.email", "must not be blank"));
    violations.add(buildViolation("createUser.param.email", "invalid format"));

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
  }

  @Test
  void should_construct_authentication_exception() {
    AuthenticationException ex = new AuthenticationException();
    assertNotNull(ex);
    assertInstanceOf(RuntimeException.class, ex);
  }
}
