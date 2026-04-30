package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user() {
    User user = new User("test@test.com", "testuser", "pass", "bio", "image");
    assertEquals("test@test.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
    assertNotNull(user.getId());
  }

  @Test
  public void should_update_email() {
    User user = new User("old@test.com", "user", "pass", "bio", "img");
    user.update("new@test.com", "", "", "", "");
    assertEquals("new@test.com", user.getEmail());
    assertEquals("user", user.getUsername());
  }

  @Test
  public void should_update_username() {
    User user = new User("e@test.com", "oldname", "pass", "bio", "img");
    user.update("", "newname", "", "", "");
    assertEquals("newname", user.getUsername());
  }

  @Test
  public void should_update_password() {
    User user = new User("e@test.com", "user", "oldpass", "bio", "img");
    user.update("", "", "newpass", "", "");
    assertEquals("newpass", user.getPassword());
  }

  @Test
  public void should_update_bio() {
    User user = new User("e@test.com", "user", "pass", "old bio", "img");
    user.update("", "", "", "new bio", "");
    assertEquals("new bio", user.getBio());
  }

  @Test
  public void should_update_image() {
    User user = new User("e@test.com", "user", "pass", "bio", "oldimg");
    user.update("", "", "", "", "newimg");
    assertEquals("newimg", user.getImage());
  }

  @Test
  public void should_not_update_with_empty_values() {
    User user = new User("e@test.com", "user", "pass", "bio", "img");
    user.update("", "", "", "", "");
    assertEquals("e@test.com", user.getEmail());
    assertEquals("user", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("img", user.getImage());
  }

  @Test
  public void should_not_update_with_null_values() {
    User user = new User("e@test.com", "user", "pass", "bio", "img");
    user.update(null, null, null, null, null);
    assertEquals("e@test.com", user.getEmail());
    assertEquals("user", user.getUsername());
  }

  @Test
  public void should_update_all_fields() {
    User user = new User("old@test.com", "old", "oldpass", "old bio", "old img");
    user.update("new@test.com", "newuser", "newpass", "new bio", "new img");
    assertEquals("new@test.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("new bio", user.getBio());
    assertEquals("new img", user.getImage());
  }

  @Test
  public void should_equal_by_id() {
    User u1 = new User("e@test.com", "user", "pass", "", "");
    User u2 = new User("e@test.com", "user", "pass", "", "");
    assertNotEquals(u1, u2);
    assertEquals(u1, u1);
  }
}
