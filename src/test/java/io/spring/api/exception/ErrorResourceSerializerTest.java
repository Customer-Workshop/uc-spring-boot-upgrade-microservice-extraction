package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ErrorResourceSerializerTest {

  private ErrorResourceSerializer serializer;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    serializer = new ErrorResourceSerializer();
    objectMapper = new ObjectMapper();
  }

  @Test
  void should_serialize_single_field_error() throws Exception {
    FieldErrorResource fieldError =
        new FieldErrorResource("user", "email", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Collections.singletonList(fieldError));

    String json = serializeToJson(errorResource);

    assertTrue(json.contains("\"errors\""));
    assertTrue(json.contains("\"email\""));
    assertTrue(json.contains("can't be empty"));
  }

  @Test
  void should_serialize_multiple_errors_for_same_field() throws Exception {
    FieldErrorResource error1 =
        new FieldErrorResource("user", "email", "NotBlank", "can't be empty");
    FieldErrorResource error2 =
        new FieldErrorResource("user", "email", "Email", "should be an email");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(error1, error2));

    String json = serializeToJson(errorResource);

    assertTrue(json.contains("\"email\""));
    assertTrue(json.contains("can't be empty"));
    assertTrue(json.contains("should be an email"));
  }

  @Test
  void should_serialize_multiple_different_field_errors() throws Exception {
    FieldErrorResource error1 =
        new FieldErrorResource("user", "email", "NotBlank", "can't be empty");
    FieldErrorResource error2 =
        new FieldErrorResource("user", "username", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(error1, error2));

    String json = serializeToJson(errorResource);

    assertTrue(json.contains("\"email\""));
    assertTrue(json.contains("\"username\""));
  }

  @Test
  void should_serialize_empty_field_errors() throws Exception {
    ErrorResource errorResource = new ErrorResource(Collections.emptyList());

    String json = serializeToJson(errorResource);

    assertTrue(json.contains("\"errors\""));
  }

  private String serializeToJson(ErrorResource errorResource) throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = objectMapper.getSerializerProvider();
    serializer.serialize(errorResource, gen, provider);
    gen.flush();
    return writer.toString();
  }
}
