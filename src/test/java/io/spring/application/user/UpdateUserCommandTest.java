package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.user.User;
import org.junit.jupiter.api.Test;

public class UpdateUserCommandTest {

  @Test
  void should_create_with_user_and_param() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    UpdateUserParam param =
        UpdateUserParam.builder().email("new@test.com").username("newname").build();

    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertEquals(user, command.getTargetUser());
    assertEquals(param, command.getParam());
  }

  @Test
  void should_get_target_user() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    UpdateUserParam param = UpdateUserParam.builder().build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertEquals("testuser", command.getTargetUser().getUsername());
    assertEquals("test@test.com", command.getTargetUser().getEmail());
  }

  @Test
  void should_get_param() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    UpdateUserParam param = UpdateUserParam.builder().email("new@test.com").bio("new bio").build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertEquals("new@test.com", command.getParam().getEmail());
    assertEquals("new bio", command.getParam().getBio());
  }
}
