package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.user.User;
import org.junit.jupiter.api.Test;

public class UpdateUserCommandTest {

  @Test
  void should_create_command_with_user_and_param() {
    User user = new User("test@test.com", "user", "pass", "bio", "image");
    UpdateUserParam param =
        UpdateUserParam.builder().email("new@test.com").username("newuser").build();

    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertEquals(user, command.getTargetUser());
    assertEquals(param, command.getParam());
  }

  @Test
  void should_hold_target_user_reference() {
    User user = new User("a@a.com", "auser", "pass", "", "");
    UpdateUserParam param = UpdateUserParam.builder().build();

    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertSame(user, command.getTargetUser());
    assertSame(param, command.getParam());
  }
}
