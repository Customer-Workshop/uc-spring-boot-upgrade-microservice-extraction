package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserWithTokenTest {

  @Test
  public void should_create_user_with_token() {
    UserData userData = new UserData("id1", "test@test.com", "testuser", "bio", "image");
    UserWithToken uwt = new UserWithToken(userData, "mytoken");

    assertEquals("test@test.com", uwt.getEmail());
    assertEquals("testuser", uwt.getUsername());
    assertEquals("bio", uwt.getBio());
    assertEquals("image", uwt.getImage());
    assertEquals("mytoken", uwt.getToken());
  }

  @Test
  public void should_create_user_with_token_null_fields() {
    UserData userData = new UserData("id1", "email", "user", null, null);
    UserWithToken uwt = new UserWithToken(userData, "token");

    assertNull(uwt.getBio());
    assertNull(uwt.getImage());
    assertEquals("token", uwt.getToken());
  }
}
