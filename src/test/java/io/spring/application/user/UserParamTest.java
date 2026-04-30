package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.user.User;
import org.junit.jupiter.api.Test;

class UserParamTest {

  @Test
  void should_create_register_param() {
    RegisterParam param = new RegisterParam("test@test.com", "testuser", "password");

    assertEquals("test@test.com", param.getEmail());
    assertEquals("testuser", param.getUsername());
    assertEquals("password", param.getPassword());
  }

  @Test
  void should_create_default_register_param() {
    RegisterParam param = new RegisterParam();

    assertNull(param.getEmail());
    assertNull(param.getUsername());
    assertNull(param.getPassword());
  }

  @Test
  void should_create_update_user_param_with_builder() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new image")
            .build();

    assertEquals("new@test.com", param.getEmail());
    assertEquals("newuser", param.getUsername());
    assertEquals("newpass", param.getPassword());
    assertEquals("new bio", param.getBio());
    assertEquals("new image", param.getImage());
  }

  @Test
  void should_create_default_update_user_param() {
    UpdateUserParam param = UpdateUserParam.builder().build();

    assertEquals("", param.getEmail());
    assertEquals("", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void should_create_update_user_param_no_args() {
    UpdateUserParam param = new UpdateUserParam();

    assertEquals("", param.getEmail());
    assertEquals("", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void should_create_update_user_param_all_args() {
    UpdateUserParam param =
        new UpdateUserParam("email@test.com", "pass", "user", "bio", "img");

    assertEquals("email@test.com", param.getEmail());
    assertEquals("pass", param.getPassword());
    assertEquals("user", param.getUsername());
  }

  @Test
  void should_create_update_user_command() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    UpdateUserParam param = UpdateUserParam.builder().email("new@test.com").build();

    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertEquals(user, command.getTargetUser());
    assertEquals(param, command.getParam());
  }
}
