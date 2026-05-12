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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GraphQLCustomizeExceptionHandlerTest {

  @InjectMocks private GraphQLCustomizeExceptionHandler handler;

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> createMockViolation(String pathStr, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation.getMessage()).thenReturn(message);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);

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
          public Class<? extends javax.validation.Payload>[] payload() {
            return new Class[0];
          }

          @Override
          public Class<? extends Annotation> annotationType() {
            return javax.validation.constraints.NotBlank.class;
          }
        };

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    return violation;
  }

  private DataFetcherExceptionHandlerParameters buildParams(Throwable ex) {
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());

    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);

    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dfe)
        .exception(ex)
        .build();
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(ex));

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_exception() {
    ConstraintViolation<?> violation =
        createMockViolation("createUser.registerParam.email", "must not be blank");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);
    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(cve));

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_generic_exception_with_default_handler() {
    RuntimeException ex = new RuntimeException("generic error");
    DataFetcherExceptionHandlerResult result = handler.onException(buildParams(ex));

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data_from_constraint_violation() {
    ConstraintViolation<?> violation = createMockViolation("email", "must not be blank");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void should_get_errors_as_data_with_empty_violations() {
    ConstraintViolationException cve =
        new ConstraintViolationException("error", Collections.emptySet());
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertTrue(error.getErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_with_simple_path() {
    ConstraintViolation<?> violation = createMockViolation("fieldName", "invalid");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertFalse(error.getErrors().isEmpty());
    assertEquals("fieldName", error.getErrors().get(0).getKey());
  }
}
