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

public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;
  private DataFetchingEnvironment dfe;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
    dfe = mock(DataFetchingEnvironment.class);
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dfe)
            .exception(new InvalidAuthenticationException())
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_exception() {
    ConstraintViolation<?> violation = mockViolation("field.param.email", "must not be blank");

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dfe)
            .exception(cve)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_generic_exception_with_default_handler() {
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dfe)
            .exception(new RuntimeException("generic error"))
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_get_errors_as_data_from_constraint_violation() {
    ConstraintViolation<?> violation = mockViolation("field.param.email", "must not be blank");

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void should_get_errors_as_data_with_multiple_violations() {
    ConstraintViolation<?> violation1 = mockViolation("field.param.email", "must not be blank");
    ConstraintViolation<?> violation2 = mockViolation("field.param.email", "should be an email");
    ConstraintViolation<?> violation3 = mockViolation("field.param.username", "can't be empty");

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation1);
    violations.add(violation2);
    violations.add(violation3);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
  }

  @Test
  void should_handle_constraint_violation_with_single_path_segment() {
    ConstraintViolation<?> violation = mockViolation("email", "must not be blank");

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dfe)
            .exception(cve)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_with_multiple_field_errors() {
    ConstraintViolation<?> violation1 = mockViolation("a.b.email", "invalid");
    ConstraintViolation<?> violation2 = mockViolation("a.b.email", "too long");

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation1);
    violations.add(violation2);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dfe)
            .exception(cve)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> mockViolation(String propertyPath, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Annotation annotation =
        new Annotation() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return Override.class;
          }
        };

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    return violation;
  }
}
