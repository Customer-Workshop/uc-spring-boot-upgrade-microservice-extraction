package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.ErrorItem;
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

  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
    lenient()
        .when(dataFetchingEnvironment.getExecutionStepInfo())
        .thenReturn(
            graphql.execution.ExecutionStepInfo.newExecutionStepInfo()
                .type(graphql.schema.GraphQLObjectType.newObject().name("Query").build())
                .path(ResultPath.rootPath())
                .build());
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
  @Test
  void should_handle_constraint_violation_exception() {
    ConstraintViolation violation = mockViolation("createUser.param.email", "can't be empty");
    Set violations = new HashSet();
    violations.add(violation);
    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);

    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(cve));
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_delegate_to_default_handler_for_other_exceptions() {
    RuntimeException exception = new RuntimeException("some error");
    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(exception));
    assertNotNull(result);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_convert_constraint_violations_to_error_data() {
    ConstraintViolation violation = mockViolation("createUser.param.email", "can't be empty");
    Set violations = new HashSet();
    violations.add(violation);
    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertFalse(error.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_convert_multiple_violations_to_error_data() {
    ConstraintViolation violation1 = mockViolation("createUser.param.email", "can't be empty");
    ConstraintViolation violation2 = mockViolation("createUser.param.username", "can't be empty");
    Set violations = new HashSet();
    violations.add(violation1);
    violations.add(violation2);
    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
    assertEquals(2, error.getErrors().size());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_group_multiple_errors_for_same_field() {
    ConstraintViolation violation1 = mockViolation("createUser.param.email", "can't be empty");
    ConstraintViolation violation2 = mockViolation("createUser.param.email", "should be an email");
    Set violations = new HashSet();
    violations.add(violation1);
    violations.add(violation2);
    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
    assertEquals(1, error.getErrors().size());
    ErrorItem item = error.getErrors().get(0);
    assertEquals("email", item.getKey());
    assertEquals(2, item.getValue().size());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_single_segment_property_path() {
    ConstraintViolation violation = mockViolation("email", "can't be empty");
    Set violations = new HashSet();
    violations.add(violation);
    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation mockViolation(String propertyPath, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(Object.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    java.lang.annotation.Annotation annotation = mock(javax.validation.constraints.NotBlank.class);
    when(annotation.annotationType())
        .thenReturn((Class) javax.validation.constraints.NotBlank.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    return violation;
  }
}
