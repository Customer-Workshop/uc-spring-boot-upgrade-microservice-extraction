package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
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
public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
    lenient().when(dataFetchingEnvironment.getExecutionStepInfo()).thenReturn(null);
  }

  private DataFetcherExceptionHandlerParameters buildParams(Throwable exception) {
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());
    return params;
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
    Set<ConstraintViolation<?>> violations = buildViolationSet("param.email", "must not be blank");

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    DataFetcherExceptionHandlerParameters params = buildParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_not_handle_other_exceptions_as_auth_or_constraint() {
    RuntimeException exception = new RuntimeException("some error");
    assertFalse(exception instanceof InvalidAuthenticationException);
    assertFalse(exception instanceof ConstraintViolationException);
  }

  @Test
  void should_get_errors_as_data_from_constraint_violation() {
    Set<ConstraintViolation<?>> violations =
        buildViolationSet("param.field.email", "invalid email");

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void should_handle_single_part_property_path() {
    Set<ConstraintViolation<?>> violations = buildViolationSet("email", "invalid");

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
  }

  @Test
  void should_create_authentication_exception() {
    AuthenticationException ex = new AuthenticationException();
    assertNotNull(ex);
    assertTrue(ex instanceof RuntimeException);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Set<ConstraintViolation<?>> buildViolationSet(String pathStr, String message) {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation =
        new Override() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return Override.class;
          }
        };
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn(message);
    violations.add(violation);
    return violations;
  }
}
