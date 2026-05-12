package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  private DataFetcherExceptionHandlerParameters buildParams(Exception exception) {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dfe)
        .exception(exception)
        .build();
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("param.field.email", "must not be blank"));
    ConstraintViolationException exception = new ConstraintViolationException(violations);
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_generic_exception() {
    RuntimeException exception = new RuntimeException("some error");
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("root.param.email", "must not be blank"));
    violations.add(createMockViolation("root.param.username", "already exists"));
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
  }

  @Test
  void should_handle_constraint_violation_with_simple_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "invalid"));
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);

    assertNotNull(error);
  }

  @Test
  void should_handle_multiple_violations_same_field() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("root.p.email", "must not be blank"));
    violations.add(createMockViolation("root.p.email", "must be valid format"));
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> createMockViolation(String pathStr, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    doReturn(String.class).when(violation).getRootBeanClass();
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation =
        new javax.validation.constraints.NotBlank() {
          @Override
          public String message() {
            return "";
          }

          @Override
          public Class<?>[] groups() {
            return new Class[0];
          }

          @Override
          @SuppressWarnings("unchecked")
          public Class<? extends javax.validation.Payload>[] payload() {
            return new Class[0];
          }

          @Override
          public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return javax.validation.constraints.NotBlank.class;
          }
        };
    doReturn(annotation).when(descriptor).getAnnotation();
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    return violation;
  }
}
