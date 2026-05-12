package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserWithTokenTest {

  @Test
  void should_create_from_user_data_and_token() {
    UserData userData = new UserData("id", "email@test.com", "user", "bio", "img");
    UserWithToken uwt = new UserWithToken(userData, "mytoken");

    assertEquals("email@test.com", uwt.getEmail());
    assertEquals("user", uwt.getUsername());
    assertEquals("bio", uwt.getBio());
    assertEquals("img", uwt.getImage());
    assertEquals("mytoken", uwt.getToken());
  }

  @Test
  void should_handle_null_fields_in_user_data() {
    UserData userData = new UserData("id", null, null, null, null);
    UserWithToken uwt = new UserWithToken(userData, "token");

    assertNull(uwt.getEmail());
    assertNull(uwt.getUsername());
    assertNull(uwt.getBio());
    assertNull(uwt.getImage());
    assertEquals("token", uwt.getToken());
  }
}
