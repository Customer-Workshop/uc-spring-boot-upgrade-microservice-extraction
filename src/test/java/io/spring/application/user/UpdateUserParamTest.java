package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UpdateUserParamTest {

  @Test
  void should_create_with_builder() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newname")
            .password("newpass")
            .bio("new bio")
            .image("new.jpg")
            .build();

    assertEquals("new@test.com", param.getEmail());
    assertEquals("newname", param.getUsername());
    assertEquals("newpass", param.getPassword());
    assertEquals("new bio", param.getBio());
    assertEquals("new.jpg", param.getImage());
  }

  @Test
  void should_have_empty_defaults_with_builder() {
    UpdateUserParam param = UpdateUserParam.builder().build();

    assertEquals("", param.getEmail());
    assertEquals("", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void should_create_with_all_args() {
    UpdateUserParam param = new UpdateUserParam("email@test.com", "pass", "user", "bio", "img");

    assertEquals("email@test.com", param.getEmail());
    assertEquals("pass", param.getPassword());
    assertEquals("user", param.getUsername());
    assertEquals("bio", param.getBio());
    assertEquals("img", param.getImage());
  }

  @Test
  void should_create_with_no_args() {
    UpdateUserParam param = new UpdateUserParam();

    assertEquals("", param.getEmail());
    assertEquals("", param.getPassword());
    assertEquals("", param.getUsername());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }
}
