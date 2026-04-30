package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class UpdateUserParamTest {

  @Test
  public void should_create_with_builder() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("test@test.com")
            .username("testuser")
            .password("newpass")
            .bio("my bio")
            .image("http://image.url")
            .build();

    assertEquals("test@test.com", param.getEmail());
    assertEquals("testuser", param.getUsername());
    assertEquals("newpass", param.getPassword());
    assertEquals("my bio", param.getBio());
    assertEquals("http://image.url", param.getImage());
  }

  @Test
  public void should_create_with_no_args_defaults() {
    UpdateUserParam param = new UpdateUserParam();
    assertNotNull(param);
    assertEquals("", param.getEmail());
    assertEquals("", param.getPassword());
    assertEquals("", param.getUsername());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  public void should_create_with_all_args_constructor() {
    UpdateUserParam param = new UpdateUserParam("e@e.com", "pass", "user", "bio", "img");
    assertEquals("e@e.com", param.getEmail());
    assertEquals("pass", param.getPassword());
    assertEquals("user", param.getUsername());
    assertEquals("bio", param.getBio());
    assertEquals("img", param.getImage());
  }

  @Test
  public void should_create_with_builder_defaults() {
    UpdateUserParam param = UpdateUserParam.builder().build();
    assertEquals("", param.getEmail());
    assertEquals("", param.getPassword());
    assertEquals("", param.getUsername());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  public void should_create_with_partial_builder() {
    UpdateUserParam param = UpdateUserParam.builder().email("new@test.com").bio("new bio").build();

    assertEquals("new@test.com", param.getEmail());
    assertEquals("new bio", param.getBio());
    assertEquals("", param.getPassword());
    assertEquals("", param.getUsername());
    assertEquals("", param.getImage());
  }
}
