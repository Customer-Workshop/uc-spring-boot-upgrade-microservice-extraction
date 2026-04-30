package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @Mock private DataFetchingEnvironment dataFetchingEnvironment;
  @Mock private ExecutionStepInfo executionStepInfo;

  @BeforeEach
  public void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  private DataFetcherExceptionHandlerParameters buildParams(Throwable ex) {
    when(dataFetchingEnvironment.getExecutionStepInfo()).thenReturn(executionStepInfo);
    when(executionStepInfo.getPath()).thenReturn(ResultPath.rootPath());
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dataFetchingEnvironment)
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
    violations.add(mockConstraintViolation("field.name", "must not be empty"));
    ConstraintViolationException cve = new ConstraintViolationException("validation", violations);

    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(cve));

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_generic_exception() {
    RuntimeException ex = new RuntimeException("generic error");
    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(ex));
    assertNotNull(result);
  }

  @Test
  public void should_get_errors_as_data_from_constraint_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(mockConstraintViolation("root.param.email", "must not be blank"));
    violations.add(mockConstraintViolation("root.param.username", "already exists"));
    ConstraintViolationException cve = new ConstraintViolationException("validation", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  public void should_get_errors_as_data_with_single_segment_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(mockConstraintViolation("field", "invalid"));
    ConstraintViolationException cve = new ConstraintViolationException("validation", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  public void should_get_errors_as_data_with_empty_violations() {
    ConstraintViolationException cve =
        new ConstraintViolationException("empty", Collections.emptySet());
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
    assertTrue(error.getErrors().isEmpty());
  }

  @Test
  public void should_handle_constraint_violation_with_single_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(mockConstraintViolation("simplename", "must not be blank"));
    ConstraintViolationException cve = new ConstraintViolationException("validation", violations);

    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(cve));
    assertNotNull(result);
  }

  @Test
  public void should_group_multiple_errors_for_same_field() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(mockConstraintViolation("root.param.email", "must not be blank"));
    violations.add(mockConstraintViolation("root.param.email", "invalid format"));
    ConstraintViolationException cve = new ConstraintViolationException("validation", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals(1, error.getErrors().size());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  public void should_create_authentication_exception() {
    AuthenticationException ex = new AuthenticationException();
    assertNotNull(ex);
    assertTrue(ex instanceof RuntimeException);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> mockConstraintViolation(String propertyPath, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(descriptor.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }

              @Override
              public String message() {
                return message;
              }

              @Override
              public Class<?>[] groups() {
                return new Class[0];
              }

              @Override
              public Class<? extends javax.validation.Payload>[] payload() {
                return new Class[0];
              }
            });
    return violation;
  }
}
