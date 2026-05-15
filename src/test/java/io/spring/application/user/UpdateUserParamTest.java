package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UpdateUserParamTest {

  @Test
  public void should_create_with_defaults() {
    UpdateUserParam param = new UpdateUserParam();
    assertEquals("", param.getEmail());
    assertEquals("", param.getPassword());
    assertEquals("", param.getUsername());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  public void should_create_with_all_args() {
    UpdateUserParam param = new UpdateUserParam("email@test.com", "pass", "user", "bio", "image");
    assertEquals("email@test.com", param.getEmail());
    assertEquals("pass", param.getPassword());
    assertEquals("user", param.getUsername());
    assertEquals("bio", param.getBio());
    assertEquals("image", param.getImage());
  }

  @Test
  public void should_build_with_builder() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("email@test.com")
            .username("user")
            .password("pass")
            .bio("bio")
            .image("image")
            .build();
    assertEquals("email@test.com", param.getEmail());
    assertEquals("user", param.getUsername());
    assertEquals("pass", param.getPassword());
    assertEquals("bio", param.getBio());
    assertEquals("image", param.getImage());
  }

  @Test
  public void should_build_with_defaults() {
    UpdateUserParam param = UpdateUserParam.builder().build();
    assertEquals("", param.getEmail());
    assertEquals("", param.getPassword());
    assertEquals("", param.getUsername());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  public void should_build_with_partial_values() {
    UpdateUserParam param = UpdateUserParam.builder().email("test@test.com").build();
    assertEquals("test@test.com", param.getEmail());
    assertEquals("", param.getPassword());
  }
}
