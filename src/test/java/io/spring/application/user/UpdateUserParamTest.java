package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UpdateUserParamTest {

  @Test
  void should_create_param_with_builder() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("test@test.com")
            .username("testuser")
            .password("password")
            .bio("my bio")
            .image("my-image.jpg")
            .build();

    assertEquals("test@test.com", param.getEmail());
    assertEquals("testuser", param.getUsername());
    assertEquals("password", param.getPassword());
    assertEquals("my bio", param.getBio());
    assertEquals("my-image.jpg", param.getImage());
  }

  @Test
  void should_create_param_with_defaults() {
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
    UpdateUserParam param = new UpdateUserParam("e@test.com", "pass", "user", "bio", "img");

    assertEquals("e@test.com", param.getEmail());
    assertEquals("pass", param.getPassword());
    assertEquals("user", param.getUsername());
    assertEquals("bio", param.getBio());
    assertEquals("img", param.getImage());
  }
}
