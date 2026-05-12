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
import javax.validation.constraints.NotBlank;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;
  private DataFetchingEnvironment dfe;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
    dfe = mock(DataFetchingEnvironment.class);
    graphql.execution.ExecutionStepInfo stepInfo = mock(graphql.execution.ExecutionStepInfo.class);
    lenient().when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    lenient().when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dfe)
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation.getMessage()).thenReturn("must not be empty");

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    doReturn(NotBlank.class).when(annotation).annotationType();
    doReturn(annotation).when(descriptor).getAnnotation();
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);

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
    RuntimeException exception = new RuntimeException("generic error");
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dfe)
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @Test
  void should_get_errors_as_data_from_constraint_violation() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation.getMessage()).thenReturn("email already exists");

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    doReturn(NotBlank.class).when(annotation).annotationType();
    doReturn(annotation).when(descriptor).getAnnotation();
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);

    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void should_handle_single_segment_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation.getMessage()).thenReturn("must not be null");

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    doReturn(NotBlank.class).when(annotation).annotationType();
    doReturn(annotation).when(descriptor).getAnnotation();
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);

    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
  }

  @Test
  void should_group_multiple_violations_by_field() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();

    ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
    when(violation1.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation1.getMessage()).thenReturn("too short");
    ConstraintDescriptor descriptor1 = mock(ConstraintDescriptor.class);
    Annotation annotation1 = mock(Annotation.class);
    doReturn(NotBlank.class).when(annotation1).annotationType();
    doReturn(annotation1).when(descriptor1).getAnnotation();
    when(violation1.getConstraintDescriptor()).thenReturn(descriptor1);
    Path path1 = mock(Path.class);
    when(path1.toString()).thenReturn("register.param.email");
    when(violation1.getPropertyPath()).thenReturn(path1);

    ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
    when(violation2.getRootBeanClass()).thenReturn((Class) String.class);
    when(violation2.getMessage()).thenReturn("already taken");
    ConstraintDescriptor descriptor2 = mock(ConstraintDescriptor.class);
    Annotation annotation2 = mock(Annotation.class);
    doReturn(NotBlank.class).when(annotation2).annotationType();
    doReturn(annotation2).when(descriptor2).getAnnotation();
    when(violation2.getConstraintDescriptor()).thenReturn(descriptor2);
    Path path2 = mock(Path.class);
    when(path2.toString()).thenReturn("register.param.email");
    when(violation2.getPropertyPath()).thenReturn(path2);

    violations.add(violation1);
    violations.add(violation2);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
  }
}
