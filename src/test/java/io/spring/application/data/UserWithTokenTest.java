package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserWithTokenTest {

  @Test
  void should_create_from_user_data_and_token() {
    UserData userData = new UserData("id", "email@test.com", "user", "bio", "img");
    UserWithToken uwt = new UserWithToken(userData, "jwt-token");

    assertEquals("email@test.com", uwt.getEmail());
    assertEquals("user", uwt.getUsername());
    assertEquals("bio", uwt.getBio());
    assertEquals("img", uwt.getImage());
    assertEquals("jwt-token", uwt.getToken());
  }

  @Test
  void should_handle_empty_fields() {
    UserData userData = new UserData("id", "", "", "", "");
    UserWithToken uwt = new UserWithToken(userData, "token");

    assertEquals("", uwt.getEmail());
    assertEquals("", uwt.getUsername());
    assertEquals("", uwt.getBio());
    assertEquals("", uwt.getImage());
    assertEquals("token", uwt.getToken());
  }

  @Test
  void should_handle_null_bio_and_image() {
    UserData userData = new UserData("id", "e@t.com", "user", null, null);
    UserWithToken uwt = new UserWithToken(userData, "token");

    assertNull(uwt.getBio());
    assertNull(uwt.getImage());
  }
}
