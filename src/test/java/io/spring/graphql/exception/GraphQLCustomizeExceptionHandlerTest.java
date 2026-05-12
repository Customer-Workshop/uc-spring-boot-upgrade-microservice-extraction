package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
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
    when(params.getPath()).thenReturn(graphql.execution.ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Path path = mock(Path.class);

    when(violation.getRootBeanClass()).thenReturn(String.class);
    when(violation.getPropertyPath()).thenReturn(path);
    when(path.toString()).thenReturn("createUser.param.email");
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    Annotation fakeAnnotation =
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
          public Class<? extends Annotation> annotationType() {
            return javax.validation.constraints.NotBlank.class;
          }
        };
    doReturn(fakeAnnotation).when(descriptor).getAnnotation();
    when(violation.getMessage()).thenReturn("can't be empty");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(cve);
    when(params.getPath()).thenReturn(graphql.execution.ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  void should_delegate_to_default_handler_for_other_exceptions() {
    RuntimeException exception = new RuntimeException("generic error");
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(graphql.execution.ResultPath.rootPath());

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_get_errors_as_data() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Path path = mock(Path.class);

    when(violation.getRootBeanClass()).thenReturn(String.class);
    when(violation.getPropertyPath()).thenReturn(path);
    when(path.toString()).thenReturn("email");
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    Annotation fakeAnnotation =
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
          public Class<? extends Annotation> annotationType() {
            return javax.validation.constraints.NotBlank.class;
          }
        };
    doReturn(fakeAnnotation).when(descriptor).getAnnotation();
    when(violation.getMessage()).thenReturn("can't be empty");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @Test
  void should_get_errors_as_data_with_empty_violations() {
    ConstraintViolationException cve = new ConstraintViolationException("error", new HashSet<>());

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertTrue(error.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void should_handle_multi_segment_property_path() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolation violation = mock(ConstraintViolation.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    Path path = mock(Path.class);

    when(violation.getRootBeanClass()).thenReturn(String.class);
    when(violation.getPropertyPath()).thenReturn(path);
    when(path.toString()).thenReturn("createUser.param.email");
    doReturn(descriptor).when(violation).getConstraintDescriptor();

    Annotation fakeAnnotation =
        new javax.validation.constraints.Email() {
          @Override
          public String message() {
            return "should be an email";
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
          public String regexp() {
            return ".*";
          }

          @Override
          public javax.validation.constraints.Pattern.Flag[] flags() {
            return new javax.validation.constraints.Pattern.Flag[0];
          }

          @Override
          public Class<? extends Annotation> annotationType() {
            return javax.validation.constraints.Email.class;
          }
        };
    doReturn(fakeAnnotation).when(descriptor).getAnnotation();
    when(violation.getMessage()).thenReturn("should be an email");
    violations.add(violation);

    ConstraintViolationException cve = new ConstraintViolationException("error", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
  }
}
