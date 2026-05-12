package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserWithTokenTest {

  @Test
  public void should_create_from_user_data_and_token() {
    UserData userData = new UserData("id", "test@test.com", "testuser", "bio", "image");
    UserWithToken userWithToken = new UserWithToken(userData, "my-token");

    assertEquals("test@test.com", userWithToken.getEmail());
    assertEquals("testuser", userWithToken.getUsername());
    assertEquals("bio", userWithToken.getBio());
    assertEquals("image", userWithToken.getImage());
    assertEquals("my-token", userWithToken.getToken());
  }

  @Test
  public void should_handle_null_fields_in_user_data() {
    UserData userData = new UserData("id", "test@test.com", "testuser", null, null);
    UserWithToken userWithToken = new UserWithToken(userData, "token");

    assertNull(userWithToken.getBio());
    assertNull(userWithToken.getImage());
    assertEquals("token", userWithToken.getToken());
  }
}
