package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.user.User;
import org.junit.jupiter.api.Test;

class UserParamTest {

  // === RegisterParam tests ===

  @Test
  void should_create_register_param_with_all_args() {
    RegisterParam param = new RegisterParam("test@example.com", "testuser", "password");

    assertEquals("test@example.com", param.getEmail());
    assertEquals("testuser", param.getUsername());
    assertEquals("password", param.getPassword());
  }

  @Test
  void should_create_register_param_with_no_args() {
    RegisterParam param = new RegisterParam();

    assertNull(param.getEmail());
    assertNull(param.getUsername());
    assertNull(param.getPassword());
  }

  // === UpdateUserParam tests ===

  @Test
  void should_create_update_user_param_with_builder() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new image")
            .build();

    assertEquals("new@example.com", param.getEmail());
    assertEquals("newuser", param.getUsername());
    assertEquals("newpass", param.getPassword());
    assertEquals("new bio", param.getBio());
    assertEquals("new image", param.getImage());
  }

  @Test
  void should_create_update_user_param_with_defaults() {
    UpdateUserParam param = UpdateUserParam.builder().build();

    assertEquals("", param.getEmail());
    assertEquals("", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void should_create_update_user_param_with_no_args() {
    UpdateUserParam param = new UpdateUserParam();

    // @NoArgsConstructor with @Builder.Default initializes fields to default values
    assertEquals("", param.getEmail());
    assertEquals("", param.getUsername());
    assertEquals("", param.getPassword());
    assertEquals("", param.getBio());
    assertEquals("", param.getImage());
  }

  @Test
  void should_create_update_user_param_with_all_args() {
    // Field order: email, password, username, bio, image
    UpdateUserParam param =
        new UpdateUserParam("email", "pass", "user", "bio", "img");

    assertEquals("email", param.getEmail());
    assertEquals("pass", param.getPassword());
    assertEquals("user", param.getUsername());
    assertEquals("bio", param.getBio());
    assertEquals("img", param.getImage());
  }

  // === UpdateUserCommand tests ===

  @Test
  void should_create_update_user_command() {
    User user = new User("test@example.com", "testuser", "pass", "bio", "image");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("newuser")
            .build();

    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertEquals(user, command.getTargetUser());
    assertEquals(param, command.getParam());
  }
}
