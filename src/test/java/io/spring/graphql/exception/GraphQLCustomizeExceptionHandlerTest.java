package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import java.util.*;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  @Test
  void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertEquals(1, result.getErrors().size());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_constraint_violation_exception() {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("createUser.param.email");
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public String message() {
                return "can't be empty";
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
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }
            });
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("can't be empty");

    Set violations = new HashSet();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(cve);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertEquals(1, result.getErrors().size());
  }

  @Test
  void should_delegate_to_default_handler_for_other_exceptions() {
    RuntimeException exception = new RuntimeException("generic error");
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_get_errors_as_data() {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public String message() {
                return "can't be empty";
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
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }
            });
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("can't be empty");

    Set violations = new HashSet();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_constraint_violation_with_simple_path() {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    when(violation.getRootBeanClass()).thenReturn((Class) String.class);

    Path path = mock(Path.class);
    when(path.toString()).thenReturn("simplefield");
    when(violation.getPropertyPath()).thenReturn(path);

    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(descriptor.getAnnotation())
        .thenReturn(
            new javax.validation.constraints.NotBlank() {
              @Override
              public String message() {
                return "required";
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
              public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return javax.validation.constraints.NotBlank.class;
              }
            });
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(violation.getMessage()).thenReturn("required");

    Set violations = new HashSet();
    violations.add(violation);
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(cve);
    when(params.getPath()).thenReturn(ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertEquals(1, result.getErrors().size());
  }

  @Test
  void should_create_authentication_exception() {
    AuthenticationException ex = new AuthenticationException();
    assertNotNull(ex);
    assertInstanceOf(RuntimeException.class, ex);
  }
}
