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
import io.spring.graphql.types.ErrorItem;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
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
    lenient().when(executionStepInfo.getPath()).thenReturn(ResultPath.rootPath());
    lenient().when(dataFetchingEnvironment.getExecutionStepInfo()).thenReturn(executionStepInfo);
  }

  private DataFetcherExceptionHandlerParameters buildParams(Throwable ex) {
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dataFetchingEnvironment)
        .exception(ex)
        .build();
  }

  @Test
  void onException_withInvalidAuthenticationException_returnsUnauthenticatedError() {
    InvalidAuthenticationException ex = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = buildParams(ex);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void onException_withConstraintViolationException_returnsBadRequestError() {
    ConstraintViolation<?> violation = createMockViolation("field.param.email", "already exists");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerParameters params = buildParams(ex);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void onException_withGenericException_delegatesToDefaultHandler() {
    RuntimeException ex = new RuntimeException("generic error");
    DataFetcherExceptionHandlerParameters params = buildParams(ex);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void getErrorsAsData_withSingleViolation_returnsErrorWithItems() {
    ConstraintViolation<?> violation = createMockViolation("field.param.email", "already exists");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(ex);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void getErrorsAsData_withMultipleViolations_returnsAllErrors() {
    ConstraintViolation<?> violation1 = createMockViolation("field.param.email", "already exists");
    ConstraintViolation<?> violation2 =
        createMockViolation("field.param.username", "already taken");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation1);
    violations.add(violation2);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(ex);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    List<ErrorItem> items = error.getErrors();
    assertTrue(items.size() >= 2);
  }

  @Test
  void getErrorsAsData_withMultipleViolationsOnSameField_groupsThem() {
    ConstraintViolation<?> violation1 = createMockViolation("field.param.email", "already exists");
    ConstraintViolation<?> violation2 = createMockViolation("field.param.email", "invalid format");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation1);
    violations.add(violation2);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(ex);

    assertNotNull(error);
    assertEquals(1, error.getErrors().size());
    assertEquals("email", error.getErrors().get(0).getKey());
    assertEquals(2, error.getErrors().get(0).getValue().size());
  }

  @Test
  void getErrorsAsData_withSimplePropertyPath_returnsOriginal() {
    ConstraintViolation<?> violation = createMockViolation("email", "already exists");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(ex);

    assertNotNull(error);
    assertEquals(1, error.getErrors().size());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  void onException_withConstraintViolationException_includesFieldErrors() {
    ConstraintViolation<?> violation1 = createMockViolation("field.param.email", "already exists");
    ConstraintViolation<?> violation2 = createMockViolation("field.param.email", "invalid format");
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(violation1);
    violations.add(violation2);
    ConstraintViolationException ex = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerParameters params = buildParams(ex);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertEquals(1, result.getErrors().size());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> createMockViolation(String propertyPath, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    Annotation annotation =
        new Override() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return Override.class;
          }
        };
    when(descriptor.getAnnotation()).thenReturn(annotation);

    return violation;
  }
}
