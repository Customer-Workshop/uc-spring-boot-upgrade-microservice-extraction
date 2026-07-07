package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GraphQLCustomizeExceptionHandlerTest {

  private final GraphQLCustomizeExceptionHandler handler = new GraphQLCustomizeExceptionHandler();

  private DataFetchingEnvironment dataFetchingEnvironmentWithPath() {
    DataFetchingEnvironment dfe = Mockito.mock(DataFetchingEnvironment.class);
    ExecutionStepInfo stepInfo = Mockito.mock(ExecutionStepInfo.class);
    when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    return dfe;
  }

  static class Sample {
    @NotBlank(message = "can't be empty")
    private String name;

    Sample(String name) {
      this.name = name;
    }
  }

  private ConstraintViolationException buildConstraintViolationException() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<Sample>> violations = validator.validate(new Sample(""));
    return new ConstraintViolationException(violations);
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dataFetchingEnvironmentWithPath())
            .exception(new InvalidAuthenticationException())
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertEquals(1, result.getErrors().size());
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dataFetchingEnvironmentWithPath())
            .exception(buildConstraintViolationException())
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertEquals(1, result.getErrors().size());
    assertNotNull(result.getErrors().get(0).getExtensions());
  }

  @Test
  public void should_delegate_other_exceptions_to_default_handler() {
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dataFetchingEnvironmentWithPath())
            .exception(new RuntimeException("boom"))
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_convert_constraint_violations_to_error_data() {
    Error error =
        GraphQLCustomizeExceptionHandler.getErrorsAsData(buildConstraintViolationException());

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals(1, error.getErrors().size());
    assertEquals("name", error.getErrors().get(0).getKey());
  }

  @Test
  public void should_return_empty_errors_for_empty_violations() {
    Error error =
        GraphQLCustomizeExceptionHandler.getErrorsAsData(
            new ConstraintViolationException(java.util.Collections.emptySet()));

    assertNotNull(error);
    assertEquals(0, error.getErrors().size());
  }
}
