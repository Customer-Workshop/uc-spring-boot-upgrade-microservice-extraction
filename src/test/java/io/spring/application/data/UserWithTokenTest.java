package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserWithTokenTest {

  @Test
  void should_create_user_with_token_from_user_data() {
    UserData userData = new UserData("id", "user@test.com", "username", "bio", "image.png");
    String token = "jwt-token-123";

    UserWithToken userWithToken = new UserWithToken(userData, token);

    assertEquals("user@test.com", userWithToken.getEmail());
    assertEquals("username", userWithToken.getUsername());
    assertEquals("bio", userWithToken.getBio());
    assertEquals("image.png", userWithToken.getImage());
    assertEquals("jwt-token-123", userWithToken.getToken());
  }

  @Test
  void should_handle_null_fields_in_user_data() {
    UserData userData = new UserData("id", null, null, null, null);
    String token = "token";

    UserWithToken userWithToken = new UserWithToken(userData, token);

    assertNull(userWithToken.getEmail());
    assertNull(userWithToken.getUsername());
    assertNull(userWithToken.getBio());
    assertNull(userWithToken.getImage());
    assertEquals("token", userWithToken.getToken());
  }

  @Test
  void should_handle_empty_fields() {
    UserData userData = new UserData("id", "", "", "", "");
    String token = "";

    UserWithToken userWithToken = new UserWithToken(userData, token);

    assertEquals("", userWithToken.getEmail());
    assertEquals("", userWithToken.getUsername());
    assertEquals("", userWithToken.getBio());
    assertEquals("", userWithToken.getImage());
    assertEquals("", userWithToken.getToken());
  }
}
