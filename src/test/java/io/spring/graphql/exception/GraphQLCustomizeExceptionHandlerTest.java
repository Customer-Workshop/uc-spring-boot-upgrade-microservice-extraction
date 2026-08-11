package io.spring.graphql.exception;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import io.spring.ValidationTestHelper;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GraphQLCustomizeExceptionHandlerTest {

  private final GraphQLCustomizeExceptionHandler handler = new GraphQLCustomizeExceptionHandler();

  private DataFetcherExceptionHandlerParameters parametersFor(Throwable throwable) {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class, RETURNS_DEEP_STUBS);
    when(dfe.getExecutionStepInfo().getPath()).thenReturn(ResultPath.rootPath());
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dfe)
        .exception(throwable)
        .build();
  }

  @Test
  public void should_return_unauthenticated_error() {
    DataFetcherExceptionHandlerResult result =
        handler.onException(parametersFor(new InvalidAuthenticationException()));

    Assertions.assertEquals(1, result.getErrors().size());
    Assertions.assertEquals(
        "UNAUTHENTICATED", result.getErrors().get(0).getExtensions().get("errorType"));
    Assertions.assertEquals("invalid email or password", result.getErrors().get(0).getMessage());
  }

  @Test
  public void should_return_bad_request_error_for_constraint_violation() {
    DataFetcherExceptionHandlerResult result =
        handler.onException(parametersFor(ValidationTestHelper.beanViolations()));

    Assertions.assertEquals(1, result.getErrors().size());
    Map<String, Object> extensions = result.getErrors().get(0).getExtensions();
    Assertions.assertEquals("BAD_REQUEST", extensions.get("errorType"));
    Assertions.assertEquals(List.of("can't be empty"), ((Map<?, ?>) extensions).get("email"));
  }

  @Test
  public void should_fallback_to_default_handler() {
    DataFetcherExceptionHandlerResult result =
        handler.onException(parametersFor(new RuntimeException("boom")));

    Assertions.assertEquals(1, result.getErrors().size());
    Assertions.assertTrue(result.getErrors().get(0).getMessage().contains("boom"));
  }

  @Test
  public void should_convert_bean_violations_to_error_data() {
    Error error =
        GraphQLCustomizeExceptionHandler.getErrorsAsData(ValidationTestHelper.beanViolations());

    Assertions.assertEquals("BAD_REQUEST", error.getMessage());
    Assertions.assertEquals(2, error.getErrors().size());
    Assertions.assertTrue(
        error.getErrors().stream().anyMatch(item -> item.getKey().equals("email")));
  }

  @Test
  public void should_strip_method_prefix_from_property_path() {
    ConstraintViolationException cve = ValidationTestHelper.methodViolations();

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    Assertions.assertTrue(
        error.getErrors().stream().anyMatch(item -> item.getKey().equals("email")),
        "expected `register.param.email` to be reported as `email`");
  }
}
