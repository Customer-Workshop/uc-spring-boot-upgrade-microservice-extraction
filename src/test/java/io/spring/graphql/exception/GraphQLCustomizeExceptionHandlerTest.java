package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
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
import org.junit.jupiter.api.Test;

public class GraphQLCustomizeExceptionHandlerTest {

  private final GraphQLCustomizeExceptionHandler handler = new GraphQLCustomizeExceptionHandler();

  private DataFetcherExceptionHandlerParameters buildParams(Throwable exception) {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class, RETURNS_DEEP_STUBS);
    ExecutionStepInfo stepInfo =
        ExecutionStepInfo.newExecutionStepInfo()
            .type(GraphQLObjectType.newObject().name("Query").build())
            .path(ResultPath.rootPath())
            .build();
    lenient().when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
    GraphQLFieldDefinition fieldDef =
        GraphQLFieldDefinition.newFieldDefinition()
            .name("test")
            .type(GraphQLObjectType.newObject().name("TestType").build())
            .build();
    lenient().when(dfe.getFieldDefinition()).thenReturn(fieldDef);
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
        .dataFetchingEnvironment(dfe)
        .exception(exception)
        .build();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolationException buildConstraintViolation(String pathStr, String message) {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn(String.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(pathStr);
    when(violation.getPropertyPath()).thenReturn(path);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(annotation.annotationType()).thenReturn((Class) Override.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn(message);
    violations.add(violation);
    return new ConstraintViolationException(violations);
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
    ConstraintViolationException cve =
        buildConstraintViolation("createUser.param.email", "can't be empty");
    DataFetcherExceptionHandlerParameters params = buildParams(cve);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_generic_exception() {
    RuntimeException exception = new RuntimeException("generic error");
    DataFetcherExceptionHandlerParameters params = buildParams(exception);

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data() {
    ConstraintViolationException cve =
        buildConstraintViolation("createUser.param.email", "email already taken");

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_get_errors_as_data_with_multiple_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();

    ConstraintViolation v1 = mock(ConstraintViolation.class);
    when(v1.getRootBeanClass()).thenReturn(String.class);
    Path p1 = mock(Path.class);
    when(p1.toString()).thenReturn("email");
    when(v1.getPropertyPath()).thenReturn(p1);
    ConstraintDescriptor d1 = mock(ConstraintDescriptor.class);
    Annotation a1 = mock(Annotation.class);
    when(a1.annotationType()).thenReturn((Class) Override.class);
    when(d1.getAnnotation()).thenReturn(a1);
    when(v1.getConstraintDescriptor()).thenReturn(d1);
    when(v1.getMessage()).thenReturn("can't be empty");
    violations.add(v1);

    ConstraintViolation v2 = mock(ConstraintViolation.class);
    when(v2.getRootBeanClass()).thenReturn(String.class);
    Path p2 = mock(Path.class);
    when(p2.toString()).thenReturn("username");
    when(v2.getPropertyPath()).thenReturn(p2);
    ConstraintDescriptor d2 = mock(ConstraintDescriptor.class);
    Annotation a2 = mock(Annotation.class);
    when(a2.annotationType()).thenReturn((Class) Override.class);
    when(d2.getAnnotation()).thenReturn(a2);
    when(v2.getConstraintDescriptor()).thenReturn(d2);
    when(v2.getMessage()).thenReturn("can't be empty");
    violations.add(v2);

    ConstraintViolationException cve = new ConstraintViolationException(violations);
    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    List<ErrorItem> items = error.getErrors();
    assertNotNull(items);
    assertTrue(items.size() >= 1);
  }

  @Test
  void should_handle_constraint_violation_with_single_path_segment() {
    ConstraintViolationException cve = buildConstraintViolation("email", "invalid");

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    List<ErrorItem> items = error.getErrors();
    assertFalse(items.isEmpty());
    assertEquals("email", items.get(0).getKey());
  }

  @Test
  void should_instantiate_authentication_exception() {
    AuthenticationException ex = new AuthenticationException();
    assertNotNull(ex);
    assertInstanceOf(RuntimeException.class, ex);
  }
}
