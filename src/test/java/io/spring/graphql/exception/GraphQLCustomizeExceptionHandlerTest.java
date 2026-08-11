package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.graphql.types.errors.ErrorType;
import graphql.GraphQLError;
import graphql.Scalars;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.ErrorItem;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  static class Bean {
    @NotBlank(message = "can't be empty")
    private String email = "";

    @NotBlank(message = "can't be empty")
    private String username = "";
  }

  @BeforeEach
  public void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  private ConstraintViolationException constraintViolationException() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<Bean>> violations = validator.validate(new Bean());
    return new ConstraintViolationException("invalid", violations);
  }

  private DataFetcherExceptionHandlerParameters parametersFor(Throwable throwable) {
    ExecutionStepInfo stepInfo =
        ExecutionStepInfo.newExecutionStepInfo()
            .type(Scalars.GraphQLString)
            .path(ResultPath.parse("/user"))
            .build();
    DataFetchingEnvironment environment =
        DataFetchingEnvironmentImpl.newDataFetchingEnvironment()
            .executionStepInfo(stepInfo)
            .mergedField(MergedField.newMergedField(Field.newField("user").build()).build())
            .build();
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .exception(throwable)
        .dataFetchingEnvironment(environment)
        .build();
  }

  @Test
  public void should_map_invalid_authentication_to_unauthenticated_error() {
    DataFetcherExceptionHandlerResult result =
        handler.onException(parametersFor(new InvalidAuthenticationException()));

    assertEquals(1, result.getErrors().size());
    GraphQLError error = result.getErrors().get(0);
    assertEquals("invalid email or password", error.getMessage());
    assertEquals(
        ErrorType.UNAUTHENTICATED.name(), error.getExtensions().get("errorType").toString());
  }

  @Test
  public void should_map_graphql_authentication_exception_to_unauthenticated_error() {
    DataFetcherExceptionHandlerResult result =
        handler.onException(parametersFor(new AuthenticationException()));

    assertEquals(1, result.getErrors().size());
    GraphQLError error = result.getErrors().get(0);
    assertEquals("unauthenticated", error.getMessage());
    assertEquals(
        ErrorType.UNAUTHENTICATED.name(), error.getExtensions().get("errorType").toString());
  }

  @Test
  public void should_map_constraint_violation_to_bad_request_with_field_extensions() {
    DataFetcherExceptionHandlerResult result =
        handler.onException(parametersFor(constraintViolationException()));

    assertEquals(1, result.getErrors().size());
    Map<String, Object> extensions = result.getErrors().get(0).getExtensions();
    assertTrue(extensions.containsKey("email"));
    assertTrue(extensions.containsKey("username"));
    assertEquals(Collections.singletonList("can't be empty"), ((List<?>) extensions.get("email")));
  }

  @Test
  public void should_delegate_unknown_exception_to_default_handler() {
    DataFetcherExceptionHandlerResult result =
        handler.onException(parametersFor(new RuntimeException("boom")));

    assertEquals(1, result.getErrors().size());
    assertTrue(result.getErrors().get(0).getMessage().contains("boom"));
  }

  @Test
  public void should_convert_violations_to_error_data() {
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(constraintViolationException());

    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals(2, error.getErrors().size());
    for (ErrorItem item : error.getErrors()) {
      assertEquals(Collections.singletonList("can't be empty"), item.getValue());
    }
  }

  @Test
  public void should_strip_method_prefix_from_nested_property_path() throws Exception {
    NotBlank annotation = Bean.class.getDeclaredField("email").getAnnotation(NotBlank.class);
    ConstraintDescriptor<?> descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenAnswer(invocation -> annotation);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.registerParam.email");
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("can't be empty");
    when(violation.getConstraintDescriptor()).thenAnswer(invocation -> descriptor);
    when(violation.getRootBeanClass()).thenAnswer(invocation -> Bean.class);

    Error error =
        GraphQLCustomizeExceptionHandler.getErrorsAsData(
            new ConstraintViolationException("invalid", Collections.singleton(violation)));

    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  public void should_return_error_with_empty_items_for_empty_violations() {
    Error error =
        GraphQLCustomizeExceptionHandler.getErrorsAsData(
            new ConstraintViolationException("invalid", Collections.emptySet()));

    assertTrue(error.getErrors().isEmpty());
  }
}
