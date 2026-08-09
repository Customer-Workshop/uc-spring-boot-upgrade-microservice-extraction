package io.spring.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomizeExceptionHandlerTest {
  private final CustomizeExceptionHandler handler = new CustomizeExceptionHandler();
  private final WebRequest request = mock(WebRequest.class);

  @Test
  void handlesInvalidRequestWithFieldErrors() {
    BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "article");
    binding.addError(
        new FieldError(
            "article",
            "title",
            null,
            false,
            new String[] {"NotBlank"},
            null,
            "must not be blank"));
    InvalidRequestException exception = new InvalidRequestException(binding);
    var response = handler.handleInvalidRequest(exception, request);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
    ErrorResource error = (ErrorResource) response.getBody();
    assertEquals(1, error.getFieldErrors().size());
    assertEquals("title", error.getFieldErrors().get(0).getField());
    assertEquals("must not be blank", error.getFieldErrors().get(0).getMessage());
  }

  @Test
  void handlesAuthenticationAndConstraintViolations() {
    var auth = handler.handleInvalidAuthentication(new InvalidAuthenticationException(), request);
    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, auth.getStatusCode());
    assertEquals(
        "invalid email or password",
        ((java.util.Map<?, ?>) auth.getBody()).get("message"));
    ErrorResource errors = handler.handleConstraintViolation(new ConstraintViolationException(Collections.emptySet()), request);
    assertTrue(errors.getFieldErrors().isEmpty());
    assertTrue(new InvalidRequestException(mock(org.springframework.validation.Errors.class)).getErrors() != null);
  }

  @Test
  void handlesInvalidRequestAndNotFoundThroughMockMvc() throws Exception {
    MockMvc mvc =
        MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(handler)
            .build();
    mvc.perform(get("/invalid").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().json("{\"errors\":{\"title\":[\"must not be blank\"]}}"));
    mvc.perform(get("/missing").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @RestController
  static class ThrowingController {
    @GetMapping("/invalid")
    String invalid() {
      BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "article");
      binding.addError(
          new FieldError(
              "article",
              "title",
              null,
              false,
              new String[] {"NotBlank"},
              null,
              "must not be blank"));
      throw new InvalidRequestException(binding);
    }

    @GetMapping("/missing")
    String missing() {
      throw new ResourceNotFoundException();
    }
  }
}
