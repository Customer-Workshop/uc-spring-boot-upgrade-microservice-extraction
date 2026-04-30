package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

public class GraphQLCustomizeExceptionHandlerTest {

  private GraphQLCustomizeExceptionHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  private DataFetchingEnvironment mockDfe() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    ExecutionStepInfo stepInfo = mock(ExecutionStepInfo.class);
    when(stepInfo.getPath()).thenReturn(ResultPath.rootPath());
    when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
    return dfe;
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();

    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(mockDfe())
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_handle_constraint_violation_exception() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(new TestConstraintViolation("createUser.param.email", "can't be empty"));
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(mockDfe())
            .exception(cve)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @Test
  public void should_get_errors_as_data() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(new TestConstraintViolation("createUser.param.email", "can't be empty"));
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertNotNull(error.getErrors());
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  public void should_handle_single_path_param() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(new TestConstraintViolation("email", "can't be empty"));
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
    assertFalse(error.getErrors().isEmpty());
    assertEquals("email", error.getErrors().get(0).getKey());
  }

  @Test
  public void should_fallback_to_default_handler_for_other_exceptions() {
    RuntimeException exception = new RuntimeException("something went wrong");

    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(mockDfe())
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertNotNull(result);
  }

  @Test
  public void should_get_errors_as_data_with_multiple_violations() {
    Set<ConstraintViolation<?>> violations = new HashSet<>();
    violations.add(new TestConstraintViolation("createUser.param.email", "can't be empty"));
    violations.add(new TestConstraintViolation("createUser.param.username", "already exists"));
    ConstraintViolationException cve = new ConstraintViolationException(violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);
    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertEquals(2, error.getErrors().size());
  }

  private static class TestConstraintViolation implements ConstraintViolation<Object> {
    private final String propertyPath;
    private final String message;

    TestConstraintViolation(String propertyPath, String message) {
      this.propertyPath = propertyPath;
      this.message = message;
    }

    @Override
    public String getMessage() {
      return message;
    }

    @Override
    public String getMessageTemplate() {
      return message;
    }

    @Override
    public Object getRootBean() {
      return null;
    }

    @Override
    public Class<Object> getRootBeanClass() {
      return Object.class;
    }

    @Override
    public Object getLeafBean() {
      return null;
    }

    @Override
    public Object[] getExecutableParameters() {
      return new Object[0];
    }

    @Override
    public Object getExecutableReturnValue() {
      return null;
    }

    @Override
    public Path getPropertyPath() {
      return new Path() {
        @Override
        public java.util.Iterator<Node> iterator() {
          return java.util.Collections.emptyIterator();
        }

        @Override
        public String toString() {
          return propertyPath;
        }
      };
    }

    @Override
    public Object getInvalidValue() {
      return null;
    }

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
      return new TestConstraintDescriptor();
    }

    @Override
    public <U> U unwrap(Class<U> type) {
      return null;
    }
  }

  private static class TestConstraintDescriptor implements ConstraintDescriptor<Annotation> {
    @Override
    public Annotation getAnnotation() {
      return new javax.validation.constraints.NotBlank() {
        @Override
        public Class<? extends Annotation> annotationType() {
          return javax.validation.constraints.NotBlank.class;
        }

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
      };
    }

    @Override
    public String getMessageTemplate() {
      return "";
    }

    @Override
    public Set<Class<?>> getGroups() {
      return new HashSet<>();
    }

    @Override
    public Set<Class<? extends javax.validation.Payload>> getPayload() {
      return new HashSet<>();
    }

    @Override
    public javax.validation.ConstraintTarget getValidationAppliesTo() {
      return null;
    }

    @Override
    public java.util.List<Class<? extends javax.validation.ConstraintValidator<Annotation, ?>>>
        getConstraintValidatorClasses() {
      return java.util.Collections.emptyList();
    }

    @Override
    public java.util.Map<String, Object> getAttributes() {
      return java.util.Collections.emptyMap();
    }

    @Override
    public Set<ConstraintDescriptor<?>> getComposingConstraints() {
      return new HashSet<>();
    }

    @Override
    public boolean isReportAsSingleViolation() {
      return false;
    }

    @Override
    public javax.validation.metadata.ValidateUnwrappedValue getValueUnwrapping() {
      return javax.validation.metadata.ValidateUnwrappedValue.DEFAULT;
    }

    @Override
    public <U> U unwrap(Class<U> type) {
      return null;
    }
  }
}
