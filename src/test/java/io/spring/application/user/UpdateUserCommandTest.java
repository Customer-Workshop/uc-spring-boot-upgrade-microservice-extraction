package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.user.User;
import org.junit.jupiter.api.Test;

public class UpdateUserCommandTest {

  @Test
  void should_create_with_user_and_param() {
    User user = new User("e@t.com", "user", "pass", "bio", "img");
    UpdateUserParam param =
        UpdateUserParam.builder().email("new@t.com").username("newuser").build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertEquals(user, command.getTargetUser());
    assertEquals(param, command.getParam());
  }

  @Test
  void should_get_target_user() {
    User user = new User("e@t.com", "user", "pass", "", "");
    UpdateUserParam param = UpdateUserParam.builder().build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertNotNull(command.getTargetUser());
    assertEquals("user", command.getTargetUser().getUsername());
  }

  @Test
  void should_get_param() {
    User user = new User("e@t.com", "user", "pass", "", "");
    UpdateUserParam param =
        UpdateUserParam.builder().email("updated@t.com").bio("updated bio").build();
    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertNotNull(command.getParam());
    assertEquals("updated@t.com", command.getParam().getEmail());
    assertEquals("updated bio", command.getParam().getBio());
  }
}
