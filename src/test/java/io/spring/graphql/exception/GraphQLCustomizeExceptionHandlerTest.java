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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;
  @Mock private ExecutionStepInfo executionStepInfo;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
    lenient().when(executionStepInfo.getPath()).thenReturn(ResultPath.rootPath());
    lenient().when(dataFetchingEnvironment.getExecutionStepInfo()).thenReturn(executionStepInfo);
  }

  @SuppressWarnings("unchecked")
  private ConstraintViolation<?> createViolation(String propertyPath, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);

    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getRootBeanClass()).thenReturn(Object.class);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getMessage()).thenReturn(message);

    return violation;
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dataFetchingEnvironment)
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("createUser.param.email", "can't be empty"));
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dataFetchingEnvironment)
            .exception(cve)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_delegate_to_default_handler_for_other_exceptions() {
    RuntimeException exception = new RuntimeException("some error");
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dataFetchingEnvironment)
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data_from_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("createUser.param.email", "duplicated email"));
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void should_handle_single_segment_property_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("email", "invalid"));
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  void should_group_multiple_violations_on_same_field() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("createUser.param.email", "can't be empty"));
    violations.add(createViolation("createUser.param.email", "duplicated email"));
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dataFetchingEnvironment)
            .exception(cve)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }
}
