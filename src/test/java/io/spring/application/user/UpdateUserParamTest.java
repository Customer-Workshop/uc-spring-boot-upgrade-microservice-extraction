package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UpdateUserParamTest {

  @Test
  void should_create_param_with_builder() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("test@test.com")
            .username("user")
            .password("pass")
            .bio("bio")
            .image("img.png")
            .build();

    assertEquals("test@test.com", param.getEmail());
    assertEquals("user", param.getUsername());
    assertEquals("pass", param.getPassword());
    assertEquals("bio", param.getBio());
    assertEquals("img.png", param.getImage());
  }

  @Test
  void should_have_empty_defaults() {
    UpdateUserParam param = UpdateUserParam.builder().build();

    assertEquals("", param.getEmail());
    assertEquals("", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void should_create_param_with_no_args() {
    UpdateUserParam param = new UpdateUserParam();

    assertEquals("", param.getEmail());
    assertEquals("", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void should_create_param_with_all_args() {
    UpdateUserParam param = new UpdateUserParam("e@e.com", "p", "u", "b", "i");

    assertEquals("e@e.com", param.getEmail());
    assertEquals("p", param.getPassword());
    assertEquals("u", param.getUsername());
    assertEquals("b", param.getBio());
    assertEquals("i", param.getImage());
  }
}
