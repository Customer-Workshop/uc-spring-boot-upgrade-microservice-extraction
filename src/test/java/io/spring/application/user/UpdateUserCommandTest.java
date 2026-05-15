package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.user.User;
import org.junit.jupiter.api.Test;

public class UpdateUserCommandTest {

  @Test
  public void should_create_update_user_command() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    UpdateUserParam param =
        new UpdateUserParam("new@test.com", "newpass", "newuser", "newbio", "newimage");
    UpdateUserCommand command = new UpdateUserCommand(user, param);
    assertEquals(user, command.getTargetUser());
    assertEquals(param, command.getParam());
  }

  @Test
  public void should_return_target_user() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    UpdateUserParam param = new UpdateUserParam();
    UpdateUserCommand command = new UpdateUserCommand(user, param);
    assertNotNull(command.getTargetUser());
    assertEquals("username", command.getTargetUser().getUsername());
  }

  @Test
  public void should_return_param() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    UpdateUserParam param =
        new UpdateUserParam("new@test.com", "newpass", "newuser", "newbio", "newimage");
    UpdateUserCommand command = new UpdateUserCommand(user, param);
    assertEquals("new@test.com", command.getParam().getEmail());
  }
}
