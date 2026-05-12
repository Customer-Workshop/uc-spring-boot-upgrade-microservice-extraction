package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
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
  private DataFetchingEnvironment dfe;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
    dfe = mock(DataFetchingEnvironment.class);
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    lenient().when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    lenient().when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
  }

  private DataFetcherExceptionHandlerParameters buildParams(Throwable ex) {
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dfe)
        .exception(ex)
        .build();
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(ex));
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("field.param.name", "must not be blank"));

    ConstraintViolationException ex =
        new ConstraintViolationException("validation failed", violations);
    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(ex));
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_constraint_violation_with_simple_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("simplename", "error msg"));

    ConstraintViolationException ex =
        new ConstraintViolationException("validation failed", violations);
    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(ex));
    assertNotNull(result);
  }

  @Test
  public void should_delegate_other_exceptions_to_default_handler() {
    RuntimeException ex = new RuntimeException("unexpected error");
    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(ex));
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_get_errors_as_data_from_constraint_violation() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("root.param.email", "invalid email"));
    violations.add(createMockViolation("root.param.email", "already taken"));
    violations.add(createMockViolation("username", "too short"));

    ConstraintViolationException ex = new ConstraintViolationException("validation", violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(ex);
    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  public void should_get_errors_as_data_empty_violations() {
    ConstraintViolationException ex =
        new ConstraintViolationException("validation", new HashSet<>());
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(ex);
    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertTrue(error.getErrors().isEmpty());
  }

  @SuppressWarnings("unchecked")
  private ConstraintViolation<?> createMockViolation(String propertyPath, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    return violation;
  }
}
