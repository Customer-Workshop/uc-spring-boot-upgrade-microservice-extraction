package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ErrorResourceSerializerTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(ErrorResource.class, new ErrorResourceSerializer());
    objectMapper.registerModule(module);
  }

  @Test
  void should_serialize_single_field_error() throws JsonProcessingException {
    FieldErrorResource fieldError =
        new FieldErrorResource("Article", "title", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fieldError));

    String json = objectMapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("title"));
    assertTrue(json.contains("can't be empty"));
  }

  @Test
  void should_serialize_multiple_field_errors() throws JsonProcessingException {
    FieldErrorResource error1 =
        new FieldErrorResource("Article", "title", "NotBlank", "can't be empty");
    FieldErrorResource error2 = new FieldErrorResource("Article", "title", "Size", "too short");
    FieldErrorResource error3 =
        new FieldErrorResource("Article", "body", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(error1, error2, error3));

    String json = objectMapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("title"));
    assertTrue(json.contains("body"));
  }

  @Test
  void should_serialize_empty_field_errors() throws JsonProcessingException {
    ErrorResource errorResource = new ErrorResource(Collections.emptyList());

    String json = objectMapper.writeValueAsString(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
  }

  @Test
  void should_create_error_resource() {
    FieldErrorResource fieldError =
        new FieldErrorResource("Article", "title", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(fieldError));

    assertNotNull(errorResource.getFieldErrors());
    assertEquals(1, errorResource.getFieldErrors().size());
  }

  @Test
  void should_create_field_error_resource() {
    FieldErrorResource fieldError =
        new FieldErrorResource("Article", "title", "NotBlank", "can't be empty");

    assertEquals("Article", fieldError.getResource());
    assertEquals("title", fieldError.getField());
    assertEquals("NotBlank", fieldError.getCode());
    assertEquals("can't be empty", fieldError.getMessage());
  }
}
