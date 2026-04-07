package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ErrorResourceSerializerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void should_serialize_error_resource_with_single_field() throws JsonProcessingException {
    FieldErrorResource fieldError =
        new FieldErrorResource("article", "title", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fieldError));

    String json = objectMapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("title"));
    assertTrue(json.contains("can't be empty"));
  }

  @Test
  public void should_serialize_error_resource_with_multiple_fields() throws JsonProcessingException {
    FieldErrorResource fieldError1 =
        new FieldErrorResource("article", "title", "NotBlank", "can't be empty");
    FieldErrorResource fieldError2 =
        new FieldErrorResource("article", "body", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fieldError1, fieldError2));

    String json = objectMapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("title"));
    assertTrue(json.contains("body"));
  }

  @Test
  public void should_serialize_error_resource_with_multiple_errors_same_field()
      throws JsonProcessingException {
    FieldErrorResource fieldError1 =
        new FieldErrorResource("article", "title", "NotBlank", "can't be empty");
    FieldErrorResource fieldError2 =
        new FieldErrorResource("article", "title", "Size", "too short");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fieldError1, fieldError2));

    String json = objectMapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("title"));
    assertTrue(json.contains("can't be empty"));
    assertTrue(json.contains("too short"));
  }

  @Test
  public void should_serialize_empty_error_resource() throws JsonProcessingException {
    ErrorResource errorResource = new ErrorResource(Collections.emptyList());

    String json = objectMapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
  }
}
