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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @Mock private DataFetchingEnvironment dfe;

  @BeforeEach
  public void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
  }

  private DataFetcherExceptionHandlerParameters buildParams(Exception exception) {
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dfe)
        .exception(exception)
        .build();
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> createViolation(String propertyPath, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    doReturn(Override.class).when(annotation).annotationType();
    when(descriptor.getAnnotation()).thenReturn(annotation);
    doReturn(descriptor).when(violation).getConstraintDescriptor();
    when(violation.getMessage()).thenReturn(message);

    return violation;
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("createUser.param.email", "email already exists"));

    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);
    DataFetcherExceptionHandlerParameters params = buildParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_generic_exception_with_default_handler() {
    RuntimeException exception = new RuntimeException("generic error");
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  public void should_get_errors_as_data_from_constraint_violation() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("createUser.param.email", "email already exists"));

    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  public void should_handle_single_segment_property_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createViolation("singlefield", "error message"));

    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertFalse(error.getErrors().isEmpty());
  }
}
