package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  void should_create_user_with_all_fields() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image.png");

    assertNotNull(user.getId());
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image.png", user.getImage());
  }

  @Test
  void should_have_unique_ids() {
    User u1 = new User("a@a.com", "user1", "pass", "", "");
    User u2 = new User("b@b.com", "user2", "pass", "", "");

    assertNotEquals(u1.getId(), u2.getId());
  }

  @Test
  void should_update_email() {
    User user = new User("old@test.com", "user", "pass", "", "");
    user.update("new@test.com", "", "", "", "");

    assertEquals("new@test.com", user.getEmail());
  }

  @Test
  void should_update_username() {
    User user = new User("test@test.com", "oldname", "pass", "", "");
    user.update("", "newname", "", "", "");

    assertEquals("newname", user.getUsername());
  }

  @Test
  void should_update_password() {
    User user = new User("test@test.com", "user", "oldpass", "", "");
    user.update("", "", "newpass", "", "");

    assertEquals("newpass", user.getPassword());
  }

  @Test
  void should_update_bio_and_image() {
    User user = new User("test@test.com", "user", "pass", "", "");
    user.update("", "", "", "new bio", "new-image.png");

    assertEquals("new bio", user.getBio());
    assertEquals("new-image.png", user.getImage());
  }

  @Test
  void should_not_update_fields_with_empty_strings() {
    User user = new User("test@test.com", "user", "pass", "bio", "img");
    user.update("", "", "", "", "");

    assertEquals("test@test.com", user.getEmail());
    assertEquals("user", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("img", user.getImage());
  }
}
