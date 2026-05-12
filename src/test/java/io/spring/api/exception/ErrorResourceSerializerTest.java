package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ErrorResourceSerializerTest {

  @Test
  public void should_serialize_error_resource() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(ErrorResource.class, new ErrorResourceSerializer());
    mapper.registerModule(module);

    ErrorResource resource =
        new ErrorResource(
            Arrays.asList(
                new FieldErrorResource("article", "title", "NotBlank", "can't be empty"),
                new FieldErrorResource("article", "title", "Size", "too short"),
                new FieldErrorResource("article", "body", "NotBlank", "can't be empty")));

    String json = mapper.writeValueAsString(resource);
    assertNotNull(json);
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("title"));
    assertTrue(json.contains("body"));
  }

  @Test
  public void should_serialize_empty_error_resource() throws Exception {
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
