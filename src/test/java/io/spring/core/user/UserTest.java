package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_all_fields() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    assertEquals("email@test.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
    assertNotNull(user.getId());
  }

  @Test
  public void should_generate_unique_id() {
    User user1 = new User("a@test.com", "user1", "pass1", "", "");
    User user2 = new User("b@test.com", "user2", "pass2", "", "");
    assertNotEquals(user1.getId(), user2.getId());
  }

  @Test
  public void should_update_email() {
    User user = new User("old@test.com", "username", "password", "bio", "image");
    user.update("new@test.com", "", "", "", "");
    assertEquals("new@test.com", user.getEmail());
    assertEquals("username", user.getUsername());
  }

  @Test
  public void should_update_username() {
    User user = new User("email@test.com", "oldname", "password", "bio", "image");
    user.update("", "newname", "", "", "");
    assertEquals("newname", user.getUsername());
    assertEquals("email@test.com", user.getEmail());
  }

  @Test
  public void should_update_password() {
    User user = new User("email@test.com", "username", "oldpass", "bio", "image");
    user.update("", "", "newpass", "", "");
    assertEquals("newpass", user.getPassword());
  }

  @Test
  public void should_update_bio() {
    User user = new User("email@test.com", "username", "password", "oldbio", "image");
    user.update("", "", "", "newbio", "");
    assertEquals("newbio", user.getBio());
  }

  @Test
  public void should_update_image() {
    User user = new User("email@test.com", "username", "password", "bio", "oldimage");
    user.update("", "", "", "", "newimage");
    assertEquals("newimage", user.getImage());
  }

  @Test
  public void should_not_update_fields_with_empty_values() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    user.update("", "", "", "", "");
    assertEquals("email@test.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  public void should_not_update_fields_with_null_values() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    user.update(null, null, null, null, null);
    assertEquals("email@test.com", user.getEmail());
    assertEquals("username", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image", user.getImage());
  }

  @Test
  public void should_update_multiple_fields_at_once() {
    User user = new User("email@test.com", "username", "password", "bio", "image");
    user.update("new@test.com", "newname", "newpass", "newbio", "newimage");
    assertEquals("new@test.com", user.getEmail());
    assertEquals("newname", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("newbio", user.getBio());
    assertEquals("newimage", user.getImage());
  }

  @Test
  public void should_use_id_for_equality() {
    User user1 = new User("email@test.com", "username", "password", "bio", "image");
    User user2 = new User("email@test.com", "username", "password", "bio", "image");
    assertNotEquals(user1, user2);
    assertEquals(user1, user1);
  }
}
