package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class UserWithTokenTest {

  @Test
  public void should_create_from_user_data_and_token() {
    UserData userData = new UserData("id", "test@test.com", "testuser", "bio", "image");
    UserWithToken userWithToken = new UserWithToken(userData, "jwt-token");

    assertNotNull(userWithToken);
    assertEquals("test@test.com", userWithToken.getEmail());
    assertEquals("testuser", userWithToken.getUsername());
    assertEquals("bio", userWithToken.getBio());
    assertEquals("image", userWithToken.getImage());
    assertEquals("jwt-token", userWithToken.getToken());
  }

  @Test
  public void should_handle_empty_fields() {
    UserData userData = new UserData("id", "e@e.com", "user", "", "");
    UserWithToken userWithToken = new UserWithToken(userData, "token");

    assertEquals("", userWithToken.getBio());
    assertEquals("", userWithToken.getImage());
  }
}
