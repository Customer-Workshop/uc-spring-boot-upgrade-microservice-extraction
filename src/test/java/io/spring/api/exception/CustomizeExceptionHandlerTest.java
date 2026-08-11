package io.spring.api.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public class CustomizeExceptionHandlerTest {

  private MockMvc mvc;

  public static class Payload {
    @NotBlank(message = "can't be empty")
    private String title;

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }
  }

  @RestController
  @RequestMapping("/test")
  static class TestController {

    @GetMapping("/invalid-request")
    public void invalidRequest() {
      Payload payload = new Payload();
      Errors errors = new BeanPropertyBindingResult(payload, "payload");
      errors.rejectValue("title", "INVALID", "can't be empty");
      throw new InvalidRequestException(errors);
    }

    @GetMapping("/invalid-authentication")
    public void invalidAuthentication() {
      throw new InvalidAuthenticationException();
    }

    @GetMapping("/constraint-violation")
    public void constraintViolation() {
      Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
      Set<ConstraintViolation<Payload>> violations = validator.validate(new Payload());
      throw new ConstraintViolationException("invalid", violations);
    }

    @PostMapping("/body")
    public String body(@Valid @RequestBody Payload payload) {
      return payload.getTitle();
    }
  }

  @BeforeEach
  public void setUp() {
    mvc =
        MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new CustomizeExceptionHandler())
            .build();
  }

  @Test
  public void should_return_422_with_field_errors_for_invalid_request() throws Exception {
    mvc.perform(get("/test/invalid-request"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors.title[0]").value("can't be empty"));
  }

  @Test
  public void should_return_422_with_message_for_invalid_authentication() throws Exception {
    mvc.perform(get("/test/invalid-authentication"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("invalid email or password"));
  }

  @Test
  public void should_return_422_for_constraint_violation() throws Exception {
    mvc.perform(get("/test/constraint-violation"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors.title[0]").value("can't be empty"));
  }

  @Test
  public void should_return_422_for_invalid_request_body() throws Exception {
    mvc.perform(
            post("/test/body").contentType(MediaType.APPLICATION_JSON).content("{\"title\": \"\"}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors.title[0]").value("can't be empty"));
  }

  @Test
  public void should_accept_valid_request_body() throws Exception {
    mvc.perform(
            post("/test/body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"a title\"}"))
        .andExpect(status().isOk());
  }

  @Test
  public void should_expose_errors_of_invalid_request_exception() throws Exception {
    Payload payload = new Payload();
    Errors errors = new BeanPropertyBindingResult(payload, "payload");
    InvalidRequestException exception = new InvalidRequestException(errors);

    org.junit.jupiter.api.Assertions.assertEquals(errors, exception.getErrors());
    org.junit.jupiter.api.Assertions.assertEquals("", exception.getMessage());
  }

  @Test
  public void should_expose_field_error_resource_properties() {
    FieldErrorResource resource = new FieldErrorResource("resource", "field", "code", "message");

    org.junit.jupiter.api.Assertions.assertEquals("resource", resource.getResource());
    org.junit.jupiter.api.Assertions.assertEquals("field", resource.getField());
    org.junit.jupiter.api.Assertions.assertEquals("code", resource.getCode());
    org.junit.jupiter.api.Assertions.assertEquals("message", resource.getMessage());
    org.junit.jupiter.api.Assertions.assertEquals(
        Collections.singletonList(resource),
        new ErrorResource(Collections.singletonList(resource)).getFieldErrors());
  }
}
