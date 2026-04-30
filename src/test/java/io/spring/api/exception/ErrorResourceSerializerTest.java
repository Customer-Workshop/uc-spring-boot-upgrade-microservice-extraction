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

  @Test
  public void should_serialize_error_resource() throws Exception {
    FieldErrorResource fer = new FieldErrorResource("resource", "email", "NotBlank", "required");
    ErrorResource error = new ErrorResource(Arrays.asList(fer));

    StringWriter writer = new StringWriter();
    ObjectMapper mapper = new ObjectMapper();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    ErrorResourceSerializer serializer = new ErrorResourceSerializer();
    serializer.serialize(error, gen, provider);
    gen.flush();

    String json = writer.toString();
    assertTrue(json.contains("errors"));
    assertTrue(json.contains("email"));
    assertTrue(json.contains("required"));
  }

  @Test
  public void should_serialize_empty_errors() throws Exception {
    ErrorResource error = new ErrorResource(Collections.emptyList());

    StringWriter writer = new StringWriter();
    ObjectMapper mapper = new ObjectMapper();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    ErrorResourceSerializer serializer = new ErrorResourceSerializer();
    serializer.serialize(error, gen, provider);
    gen.flush();

    String json = writer.toString();
    assertTrue(json.contains("errors"));
  }

  @Test
  public void should_group_multiple_errors_for_same_field() throws Exception {
    FieldErrorResource fer1 = new FieldErrorResource("r", "email", "NotBlank", "required");
    FieldErrorResource fer2 = new FieldErrorResource("r", "email", "Email", "invalid");
    ErrorResource error = new ErrorResource(Arrays.asList(fer1, fer2));

    StringWriter writer = new StringWriter();
    ObjectMapper mapper = new ObjectMapper();
    JsonGenerator gen = new JsonFactory().createGenerator(writer);
    SerializerProvider provider = mapper.getSerializerProvider();

    ErrorResourceSerializer serializer = new ErrorResourceSerializer();
    serializer.serialize(error, gen, provider);
    gen.flush();

    String json = writer.toString();
    assertTrue(json.contains("email"));
    assertTrue(json.contains("required"));
    assertTrue(json.contains("invalid"));
  }
}
