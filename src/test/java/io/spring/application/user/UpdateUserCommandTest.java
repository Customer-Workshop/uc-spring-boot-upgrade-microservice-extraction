package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.user.User;
import org.junit.jupiter.api.Test;

class UpdateUserCommandTest {

  @Test
  void should_create_command_with_user_and_param() {
    User user = new User("test@test.com", "testuser", "pass", "bio", "image");
    UpdateUserParam param =
        UpdateUserParam.builder().email("new@test.com").username("newuser").build();

    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertEquals(user, command.getTargetUser());
    assertEquals(param, command.getParam());
    assertEquals("new@test.com", command.getParam().getEmail());
    assertEquals("newuser", command.getParam().getUsername());
  }
}
