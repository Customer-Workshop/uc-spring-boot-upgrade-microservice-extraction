package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.InvalidAuthenticationException;
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
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
    lenient().when(dataFetchingEnvironment.getExecutionStepInfo()).thenReturn(executionStepInfo);
    lenient().when(executionStepInfo.getPath()).thenReturn(ResultPath.rootPath());
  }

  private DataFetcherExceptionHandlerParameters buildParams(Throwable exception) {
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dataFetchingEnvironment)
        .exception(exception)
        .build();
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();

    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(exception));

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> buildViolation(String path, String message, Class<?> annotation) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    Path propertyPath = mock(Path.class);
    when(propertyPath.toString()).thenReturn(path);
    doReturn(propertyPath).when(violation).getPropertyPath();
    doReturn(message).when(violation).getMessage();
    doReturn(String.class).when(violation).getRootBeanClass();

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    java.lang.annotation.Annotation ann =
        mock(annotation.asSubclass(java.lang.annotation.Annotation.class));
    doReturn(annotation).when(ann).annotationType();
    doReturn(ann).when(descriptor).getAnnotation();
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    return violation;
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(
        buildViolation(
            "createUser.param.email",
            "can't be empty",
            javax.validation.constraints.NotBlank.class));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(cve));

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_delegate_to_default_handler_for_other_exceptions() {
    RuntimeException exception = new RuntimeException("generic error");

    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(exception));

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data_from_constraint_violation() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(
        buildViolation(
            "param.email", "should be an email", javax.validation.constraints.Email.class));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    io.spring.graphql.types.Error errorData = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(errorData);
    assertEquals("BAD_REQUEST", errorData.getMessage());
    assertFalse(errorData.getErrors().isEmpty());
  }

  @Test
  void should_handle_single_segment_property_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(
        buildViolation("email", "can't be empty", javax.validation.constraints.NotBlank.class));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    io.spring.graphql.types.Error errorData = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(errorData);
    assertFalse(errorData.getErrors().isEmpty());
  }

  @Test
  void should_handle_multiple_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(
        buildViolation(
            "param.email", "can't be empty", javax.validation.constraints.NotBlank.class));
    violations.add(
        buildViolation(
            "param.username", "can't be empty", javax.validation.constraints.NotBlank.class));

    ConstraintViolationException cve = new ConstraintViolationException(violations);

    io.spring.graphql.types.Error errorData = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(errorData);
    assertFalse(errorData.getErrors().isEmpty());
  }
}
