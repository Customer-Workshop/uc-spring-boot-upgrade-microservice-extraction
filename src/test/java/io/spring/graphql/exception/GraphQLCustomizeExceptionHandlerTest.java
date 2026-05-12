package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
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
  public void setUp() {
    handler = new GraphQLCustomizeExceptionHandler();
  }

  private DataFetchingEnvironment createMockDfe() {
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    MergedField mergedField = MergedField.newMergedField().addField(new Field("test")).build();
    lenient().when(dfe.getMergedField()).thenReturn(mergedField);
    lenient()
        .when(dfe.getFieldDefinition())
        .thenReturn(
            GraphQLFieldDefinition.newFieldDefinition()
                .name("test")
                .type(GraphQLObjectType.newObject().name("String").build())
                .build());
    ExecutionStepInfo stepInfo =
        ExecutionStepInfo.newExecutionStepInfo()
            .type(GraphQLObjectType.newObject().name("Test").build())
            .path(ResultPath.rootPath().segment("test"))
            .build();
    lenient().when(dfe.getExecutionStepInfo()).thenReturn(stepInfo);
    return dfe;
  }

  @Test
  public void should_handle_invalid_authentication_exception() {
    InvalidAuthenticationException exception = new InvalidAuthenticationException();
    DataFetchingEnvironment dfe = createMockDfe();
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dfe)
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
    assertFalse(result.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void should_handle_constraint_violation_exception() {
    Set violations = new HashSet();
    violations.add(mockConstraintViolation("field.param.email", "is invalid"));
    ConstraintViolationException exception =
        new ConstraintViolationException("validation failed", violations);

    DataFetchingEnvironment dfe = createMockDfe();
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
  public void should_handle_generic_exception_via_default_handler() {
    RuntimeException exception = new RuntimeException("generic error");
    DataFetchingEnvironment dfe = createMockDfe();
    DataFetcherExceptionHandlerParameters params =
        DataFetcherExceptionHandlerParameters.newExceptionParameters()
            .dataFetchingEnvironment(dfe)
            .exception(exception)
            .build();

    DataFetcherExceptionHandlerResult result = handler.onException(params);

    assertNotNull(result);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void should_get_errors_as_data_from_constraint_violation() {
    Set violations = new HashSet();
    violations.add(mockConstraintViolation("field.param.email", "is invalid"));
    violations.add(mockConstraintViolation("simple", "must not be blank"));
    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertEquals("BAD_REQUEST", error.getMessage());
    assertFalse(error.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void should_handle_constraint_violation_with_single_segment_path() {
    Set violations = new HashSet();
    violations.add(mockConstraintViolation("email", "is invalid"));
    ConstraintViolationException cve =
        new ConstraintViolationException("validation failed", violations);

    Error error = GraphQLCustomizeExceptionHandler.getErrorsAsData(cve);

    assertNotNull(error);
    assertFalse(error.getErrors().isEmpty());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ConstraintViolation mockConstraintViolation(String propertyPath, String message) {
    ConstraintViolation violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn(propertyPath);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    when(violation.getRootBeanClass()).thenReturn(String.class);
    ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    java.lang.annotation.Annotation annotation =
        new javax.validation.constraints.NotBlank() {
          @Override
          public String message() {
            return message;
          }

          @Override
          public Class[] groups() {
            return new Class[0];
          }

          @Override
          public Class[] payload() {
            return new Class[0];
          }

          @Override
          public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return javax.validation.constraints.NotBlank.class;
          }
        };
    when(descriptor.getAnnotation()).thenReturn(annotation);
    return violation;
  }
}
