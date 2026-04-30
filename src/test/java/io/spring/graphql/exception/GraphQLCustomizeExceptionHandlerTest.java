package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import io.spring.graphql.types.ErrorItem;
import java.lang.annotation.Annotation;
import java.util.Collections;
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

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  private DataFetcherExceptionHandlerParameters buildParams(Throwable exception) {
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dfe)
        .exception(exception)
        .build();
  }

  @Test
  void onException_invalidAuthentication_returnsUnauthenticatedError() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
    assertEquals(1, result.getErrors().size());
  }

  @Test
  void onException_constraintViolation_returnsBadRequestError() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = createMockViolation("email", "must not be empty");
    violations.add(violation);
    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);
    DataFetcherExceptionHandlerParameters params = buildParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
    assertEquals(1, result.getErrors().size());
  }

  @Test
  void onException_otherException_delegatesToDefaultHandler() {
    RuntimeException exception = new RuntimeException("unexpected error");
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void getErrorsAsData_withSingleViolation_returnsError() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "already exists"));
    ConstraintViolationException cve = new ConstraintViolationException("fail", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void getErrorsAsData_withMultipleViolations_groupsByField() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolation("email", "already exists"));
    violations.add(createMockViolation("email", "invalid format"));
    violations.add(createMockViolation("username", "already exists"));
    ConstraintViolationException cve = new ConstraintViolationException("fail", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    boolean hasEmailField = false;
    boolean hasUsernameField = false;
    for (ErrorItem item : error.getErrors()) {
      if ("email".equals(item.getKey())) {
        hasEmailField = true;
      }
      if ("username".equals(item.getKey())) {
        hasUsernameField = true;
      }
    }
    assertTrue(hasEmailField);
    assertTrue(hasUsernameField);
  }

  @Test
  void getErrorsAsData_withEmptyViolations_returnsEmptyErrors() {
    ConstraintViolationException cve =
        new ConstraintViolationException("fail", Collections.emptySet());

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertTrue(error.getErrors().isEmpty());
  }

  @Test
  void onException_constraintViolation_withNestedPropertyPath_extractsParam() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolationWithPath("service.param.email", "invalid"));
    ConstraintViolationException cve = new ConstraintViolationException("validation", violations);
    DataFetcherExceptionHandlerParameters params = buildParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void onException_constraintViolation_withSimplePropertyPath() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolationWithPath("email", "invalid"));
    ConstraintViolationException cve = new ConstraintViolationException("validation", violations);
    DataFetcherExceptionHandlerParameters params = buildParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void getErrorsAsData_withNestedPath_extractsCorrectField() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolationWithPath("service.param.email", "invalid"));
    ConstraintViolationException cve = new ConstraintViolationException("fail", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    boolean foundEmailField = false;
    for (ErrorItem item : error.getErrors()) {
      if ("email".equals(item.getKey())) {
        foundEmailField = true;
      }
    }
    assertTrue(foundEmailField);
  }

  @Test
  void getErrorsAsData_withSimplePath_usesFullPath() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(createMockViolationWithPath("email", "invalid"));
    ConstraintViolationException cve = new ConstraintViolationException("fail", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    boolean foundField = false;
    for (ErrorItem item : error.getErrors()) {
      if ("email".equals(item.getKey())) {
        foundField = true;
      }
    }
    assertTrue(foundField);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> createMockViolation(String field, String message) {
    return createMockViolationWithPath("service.param." + field, message);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation<?> createMockViolationWithPath(String pathStr, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    return violation;
  }
}
