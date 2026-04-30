package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ErrorResourceSerializerTest {

  @Test
  void should_serialize_error_resource() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(ErrorResource.class, new ErrorResourceSerializer());
    mapper.registerModule(module);

    ErrorResource resource =
        new ErrorResource(
            Arrays.asList(
                new FieldErrorResource("User", "email", "NotBlank", "must not be blank"),
                new FieldErrorResource("User", "username", "Size", "must be between 1 and 255")));

    String json = mapper.writeValueAsString(resource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("email"));
    assertTrue(json.contains("must not be blank"));
    assertTrue(json.contains("username"));
  }

  @Test
  void should_serialize_multiple_errors_same_field() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(ErrorResource.class, new ErrorResourceSerializer());
    mapper.registerModule(module);

    ErrorResource resource =
        new ErrorResource(
            Arrays.asList(
                new FieldErrorResource("User", "email", "NotBlank", "must not be blank"),
                new FieldErrorResource("User", "email", "Email", "invalid format")));

    String json = mapper.writeValueAsString(resource);

    assertNotNull(json);
    assertTrue(json.contains("must not be blank"));
    assertTrue(json.contains("invalid format"));
  }

  @Test
  void should_serialize_empty_errors() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(ErrorResource.class, new ErrorResourceSerializer());
    mapper.registerModule(module);

    ErrorResource resource = new ErrorResource(Arrays.asList());

    String json = mapper.writeValueAsString(resource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
  }
}
