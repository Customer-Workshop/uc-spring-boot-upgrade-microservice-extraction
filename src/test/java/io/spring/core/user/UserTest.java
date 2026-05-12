package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");

    assertNotNull(user.getId());
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  public void should_create_with_no_args_constructor() {
    User user = new User();

    assertNull(user.getId());
    assertNull(user.getEmail());
  }

  @Test
  public void should_update_email() {
    User user = new User("old@test.com", "user", "pass", "bio", "image");

    user.update("new@test.com", "", "", "", "");

    assertEquals("new@test.com", user.getEmail());
    assertEquals("user", user.getUsername());
  }

  @Test
  public void should_update_username() {
    User user = new User("email@test.com", "olduser", "pass", "bio", "image");

    user.update("", "newuser", "", "", "");

    assertEquals("newuser", user.getUsername());
    assertEquals("email@test.com", user.getEmail());
  }

  @Test
  public void should_update_password() {
    User user = new User("email@test.com", "user", "oldpass", "bio", "image");

    user.update("", "", "newpass", "", "");

    assertEquals("newpass", user.getPassword());
  }

  @Test
  public void should_update_bio() {
    User user = new User("email@test.com", "user", "pass", "old bio", "image");

    user.update("", "", "", "new bio", "");

    assertEquals("new bio", user.getBio());
  }

  @Test
  public void should_update_image() {
    User user = new User("email@test.com", "user", "pass", "bio", "old-image");

    user.update("", "", "", "", "new-image");

    assertEquals("new-image", user.getImage());
  }

  @Test
  public void should_update_all_fields() {
    User user = new User("old@test.com", "olduser", "oldpass", "old bio", "old-image");

    user.update("new@test.com", "newuser", "newpass", "new bio", "new-image");

    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("new bio", user.getBio());
    assertEquals("new-image", user.getImage());
  }

  @Test
  public void should_not_update_with_empty_values() {
    User user = new User("email@test.com", "user", "pass", "bio", "image");

    user.update("", "", "", "", "");

    assertEquals("email@test.com", user.getEmail());
    assertEquals("user", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  public void should_not_update_with_null_values() {
    User user = new User("email@test.com", "user", "pass", "bio", "image");

    user.update(null, null, null, null, null);

    assertEquals("email@test.com", user.getEmail());
    assertEquals("user", user.getUsername());
  }

  @Test
  public void should_implement_equals_by_id() {
    User user1 = new User("email@test.com", "user1", "pass", "bio", "image");
    User user2 = new User("email@test.com", "user2", "pass", "bio", "image");

    assertNotEquals(user1, user2);
    assertEquals(user1, user1);
  }

  @Test
  public void should_implement_hashcode_by_id() {
    User user = new User("email@test.com", "user", "pass", "bio", "image");

    assertEquals(user.hashCode(), user.hashCode());
  }
}
