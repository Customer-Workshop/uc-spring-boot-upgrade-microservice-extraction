package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ErrorResourceSerializerTest {

  @Test
  public void should_serialize_error_resource() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(ErrorResource.class, new ErrorResourceSerializer());
    mapper.registerModule(module);

    ErrorResource errorResource =
        new ErrorResource(
            Arrays.asList(
                new FieldErrorResource("object", "title", "NotBlank", "must not be blank"),
                new FieldErrorResource("object", "body", "NotBlank", "must not be blank")));

    String json = mapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("title"));
    assertTrue(json.contains("body"));
    assertTrue(json.contains("must not be blank"));
  }

  @Test
  public void should_serialize_error_resource_with_multiple_errors_same_field()
      throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(ErrorResource.class, new ErrorResourceSerializer());
    mapper.registerModule(module);

    ErrorResource errorResource =
        new ErrorResource(
            Arrays.asList(
                new FieldErrorResource("object", "email", "NotBlank", "must not be blank"),
                new FieldErrorResource("object", "email", "Email", "must be a valid email")));

    String json = mapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("email"));
    assertTrue(json.contains("must not be blank"));
    assertTrue(json.contains("must be a valid email"));
  }
}
