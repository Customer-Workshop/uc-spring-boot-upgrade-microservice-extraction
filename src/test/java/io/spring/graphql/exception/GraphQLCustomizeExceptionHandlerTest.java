package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
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

  private DataFetchingEnvironment mockDfe() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getExecutionStepInfo()).thenReturn(null);
    return dfe;
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getExecutionStepInfo()).thenReturn(mock(graphql.execution.ExecutionStepInfo.class));
    when(dfe.getExecutionStepInfo().getPath()).thenReturn(ResultPath.rootPath());
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dfe)
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> createMockViolation(String pathStr, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);

    when(path.toString()).thenReturn(pathStr);
    doReturn(path).when(violation).getPropertyPath();
    doReturn((Class) String.class).when(violation).getRootBeanClass();
    doReturn(message).when(violation).getMessage();
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    java.lang.annotation.Annotation annotation = mock(java.lang.annotation.Annotation.class);
    doReturn(Override.class).when(annotation).annotationType();
    doReturn(annotation).when(descriptor).getAnnotation();

    return violation;
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createUser.param.email", "email already exists"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getExecutionStepInfo()).thenReturn(mock(graphql.execution.ExecutionStepInfo.class));
    when(dfe.getExecutionStepInfo().getPath()).thenReturn(ResultPath.rootPath());
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dfe)
            .exception(cve)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_generic_exception_with_default_handler() {
    RuntimeException exception = new RuntimeException("unexpected error");
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getExecutionStepInfo()).thenReturn(mock(graphql.execution.ExecutionStepInfo.class));
    when(dfe.getExecutionStepInfo().getPath()).thenReturn(ResultPath.rootPath());
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dfe)
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data_from_constraint_violation() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createUser.param.email", "email already exists"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void should_handle_single_segment_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "invalid email"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
  }

  @Test
  void should_aggregate_multiple_violations_for_same_field() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createUser.param.email", "can't be empty"));
    violations.add(createMockViolation("createUser.param.email", "should be an email"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
  }

  @Test
  void should_handle_multiple_different_field_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("createUser.param.email", "can't be empty"));
    violations.add(createMockViolation("createUser.param.username", "can't be empty"));

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertTrue(error.getErrors().size() >= 2);
  }
}
