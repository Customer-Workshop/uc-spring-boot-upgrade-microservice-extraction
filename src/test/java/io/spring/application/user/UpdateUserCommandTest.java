package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.spring.core.user.User;
import org.junit.jupiter.api.Test;

public class UpdateUserCommandTest {

  @Test
  public void should_create_with_user_and_param() {
    User user = new User("test@test.com", "testuser", "123", "bio", "image");
    UpdateUserParam param =
        UpdateUserParam.builder().email("new@test.com").username("newuser").build();

    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertNotNull(command);
    assertEquals(user, command.getTargetUser());
    assertEquals(param, command.getParam());
    assertEquals("new@test.com", command.getParam().getEmail());
    assertEquals("newuser", command.getParam().getUsername());
  }
}
