package io.spring.graphql.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.graphql.types.Error;
import java.lang.annotation.Annotation;
import java.util.Collections;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;

class GraphQLCustomizeExceptionHandlerTest {
  private final GraphQLCustomizeExceptionHandler handler = new GraphQLCustomizeExceptionHandler();

  @Test
  void handlesAuthenticationAndDefaultExceptions() {
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(new InvalidAuthenticationException());
    when(params.getPath()).thenReturn(mock(ResultPath.class));
    DataFetcherExceptionHandlerResult result = handler.onException(params);
    assertEquals(1, result.getErrors().size());

    when(params.getException()).thenReturn(new IllegalStateException("bad"));
    assertNotNull(handler.onException(params));
  }

  @Test
  void convertsConstraintViolationsToGraphqlData() {
    var violation = mock(javax.validation.ConstraintViolation.class);
    var path = mock(javax.validation.Path.class);
    var descriptor = mock(javax.validation.metadata.ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(path.toString()).thenReturn("a.b.email");
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(annotation.annotationType()).thenReturn((Class) Deprecated.class);
    when(violation.getMessage()).thenReturn("invalid");
    ConstraintViolationException exception =
        new ConstraintViolationException(
            "bad", Collections.singleton((javax.validation.ConstraintViolation<?>) violation));
    Error data = GraphQLCustomizeExceptionHandler.getErrorsAsData(exception);
    assertEquals("BAD_REQUEST", data.getMessage());
    assertEquals(1, data.getErrors().size());
    assertEquals("email", data.getErrors().get(0).getKey());
  }

  @Test
  void handlesConstraintViolationExceptions() {
    var violation = mock(javax.validation.ConstraintViolation.class);
    var path = mock(javax.validation.Path.class);
    var descriptor = mock(javax.validation.metadata.ConstraintDescriptor.class);
    Annotation annotation = mock(Annotation.class);
    when(path.toString()).thenReturn("email");
    when(violation.getRootBeanClass()).thenReturn((Class) Object.class);
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getConstraintDescriptor()).thenReturn(descriptor);
    when(annotation.annotationType()).thenReturn((Class) Deprecated.class);
    when(descriptor.getAnnotation()).thenReturn(annotation);
    when(violation.getMessage()).thenReturn("invalid");
    ConstraintViolationException exception =
        new ConstraintViolationException(
            "bad", Collections.singleton((javax.validation.ConstraintViolation<?>) violation));
    DataFetcherExceptionHandlerParameters params =
        mock(DataFetcherExceptionHandlerParameters.class);
    when(params.getException()).thenReturn(exception);
    when(params.getPath()).thenReturn(mock(ResultPath.class));
    assertEquals(1, handler.onException(params).getErrors().size());
  }
}
