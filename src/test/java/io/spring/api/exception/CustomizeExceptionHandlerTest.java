package io.spring.api.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.spring.ValidationTestHelper;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.web.bind.annotation.RestController;

public class CustomizeExceptionHandlerTest {

  static class Payload {
    @NotBlank(message = "can't be empty")
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @RestController
  static class TestController {
    @GetMapping("/invalid-request")
    public void invalidRequest() {
      Errors errors = new BeanPropertyBindingResult(new Payload(), "payload");
      errors.rejectValue("name", "NotBlank", "can't be empty");
      throw new InvalidRequestException(errors);
    }

    @GetMapping("/invalid-authentication")
    public void invalidAuthentication() {
      throw new InvalidAuthenticationException();
    }

    @GetMapping("/constraint-violation")
    public void constraintViolation() {
      throw ValidationTestHelper.methodViolations();
    }

    @GetMapping("/bean-constraint-violation")
    public void beanConstraintViolation() {
      throw ValidationTestHelper.beanViolations();
    }

    @PostMapping("/valid-body")
    public void validBody(@Valid @RequestBody Payload payload) {}
  }

  private MockMvc mvc;

  @BeforeEach
  public void setUp() {
    mvc =
        MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new CustomizeExceptionHandler())
            .build();
  }

  @Test
  public void should_return_422_for_invalid_request() throws Exception {
    mvc.perform(get("/invalid-request"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors.name[0]").value("can't be empty"));
  }

  @Test
  public void should_return_422_for_invalid_authentication() throws Exception {
    mvc.perform(get("/invalid-authentication"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("invalid email or password"));
  }

  @Test
  public void should_return_422_for_constraint_violation() throws Exception {
    mvc.perform(get("/constraint-violation"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors.email[0]").value("can't be empty"));
  }

  @Test
  public void should_return_422_for_invalid_body() throws Exception {
    mvc.perform(
            post("/valid-body").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors.name[0]").value("can't be empty"));
  }

  @Test
  public void should_keep_simple_property_path_as_field_name() throws Exception {
    mvc.perform(get("/bean-constraint-violation"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.errors.email[0]").value("can't be empty"))
        .andExpect(jsonPath("$.errors.username[0]").value("can't be empty"));
  }

  @Test
  public void should_expose_field_error_details() {
    FieldErrorResource resource =
        new FieldErrorResource("payload", "name", "NotBlank", "can't be empty");

    Assertions.assertEquals("payload", resource.getResource());
    Assertions.assertEquals("name", resource.getField());
    Assertions.assertEquals("NotBlank", resource.getCode());
    Assertions.assertEquals("can't be empty", resource.getMessage());
  }

  @Test
  public void should_accept_valid_body() throws Exception {
    mvc.perform(
            post("/valid-body")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"a name\"}"))
        .andExpect(status().isOk());
  }
}
