package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UpdateUserParamTest {

  @Test
  void should_create_with_builder() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("e@t.com")
            .username("user")
            .password("pass")
            .bio("bio")
            .image("img")
            .build();

    assertEquals("e@t.com", param.getEmail());
    assertEquals("user", param.getUsername());
    assertEquals("pass", param.getPassword());
    assertEquals("bio", param.getBio());
    assertEquals("img", param.getImage());
  }

  @Test
  void should_create_with_defaults() {
    UpdateUserParam param = UpdateUserParam.builder().build();
    assertEquals("", param.getEmail());
    assertEquals("", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void should_create_with_no_args() {
    UpdateUserParam param = new UpdateUserParam();
    assertEquals("", param.getEmail());
    assertEquals("", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void should_create_with_all_args_constructor() {
    UpdateUserParam param = new UpdateUserParam("e@t.com", "pass", "user", "bio", "img");
    assertEquals("e@t.com", param.getEmail());
    assertEquals("pass", param.getPassword());
    assertEquals("user", param.getUsername());
    assertEquals("bio", param.getBio());
    assertEquals("img", param.getImage());
  }
}
