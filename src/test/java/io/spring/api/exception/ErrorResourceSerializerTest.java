package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ErrorResourceSerializerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void should_serialize_error_resource_with_single_error() throws Exception {
    FieldErrorResource field =
        new FieldErrorResource("User", "email", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Collections.singletonList(field));

    String json = serialize(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("email"));
    assertTrue(json.contains("can't be empty"));
  }

  @Test
  public void should_serialize_error_resource_with_multiple_errors_same_field() throws Exception {
    FieldErrorResource field1 =
        new FieldErrorResource("User", "email", "NotBlank", "can't be empty");
    FieldErrorResource field2 =
        new FieldErrorResource("User", "email", "Email", "must be valid email");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(field1, field2));

    String json = serialize(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("email"));
    assertTrue(json.contains("can't be empty"));
    assertTrue(json.contains("must be valid email"));
  }

  @Test
  public void should_serialize_error_resource_with_multiple_fields() throws Exception {
    FieldErrorResource field1 =
        new FieldErrorResource("User", "email", "NotBlank", "can't be empty");
    FieldErrorResource field2 =
        new FieldErrorResource("User", "username", "NotBlank", "can't be empty");
    ErrorResource errorResource = new ErrorResource(Arrays.asList(field1, field2));

    String json = serialize(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("email"));
    assertTrue(json.contains("username"));
  }

  @Test
  public void should_serialize_error_resource_with_empty_errors() throws Exception {
    ErrorResource errorResource = new ErrorResource(Collections.emptyList());

    String json = serialize(errorResource);

    assertNotNull(json);
    assertTrue(json.contains("errors"));
  }

  private String serialize(ErrorResource errorResource) throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = objectMapper.getSerializerProvider();
    new ErrorResourceSerializer().serialize(errorResource, gen, provider);
    gen.flush();
    return writer.toString();
  }
}
