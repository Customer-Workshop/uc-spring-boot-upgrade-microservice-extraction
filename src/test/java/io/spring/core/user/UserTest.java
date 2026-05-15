package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  void should_create_user_with_all_fields() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image.png");

    assertNotNull(user.getId());
    assertEquals("test@example.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("image.png", user.getImage());
  }

  @Test
  void should_generate_unique_ids() {
    User user1 = new User("a@b.com", "user1", "pass1", "", "");
    User user2 = new User("c@d.com", "user2", "pass2", "", "");

    assertNotEquals(user1.getId(), user2.getId());
  }

  @Test
  void should_update_email_when_not_empty() {
    User user = new User("old@example.com", "user", "pass", "bio", "img");
    user.update("new@example.com", "", "", "", "");

    assertEquals("new@example.com", user.getEmail());
    assertEquals("user", user.getUsername());
  }

  @Test
  void should_update_username_when_not_empty() {
    User user = new User("a@b.com", "oldname", "pass", "bio", "img");
    user.update("", "newname", "", "", "");

    assertEquals("newname", user.getUsername());
    assertEquals("a@b.com", user.getEmail());
  }

  @Test
  void should_update_password_when_not_empty() {
    User user = new User("a@b.com", "user", "oldpass", "bio", "img");
    user.update("", "", "newpass", "", "");

    assertEquals("newpass", user.getPassword());
  }

  @Test
  void should_update_bio_when_not_empty() {
    User user = new User("a@b.com", "user", "pass", "oldbio", "img");
    user.update("", "", "", "newbio", "");

    assertEquals("newbio", user.getBio());
  }

  @Test
  void should_update_image_when_not_empty() {
    User user = new User("a@b.com", "user", "pass", "bio", "oldimg");
    user.update("", "", "", "", "newimg");

    assertEquals("newimg", user.getImage());
  }

  @Test
  void should_not_update_fields_when_empty() {
    User user = new User("a@b.com", "user", "pass", "bio", "img");
    user.update("", "", "", "", "");

    assertEquals("a@b.com", user.getEmail());
    assertEquals("user", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("img", user.getImage());
  }

  @Test
  void should_not_update_fields_when_null() {
    User user = new User("a@b.com", "user", "pass", "bio", "img");
    user.update(null, null, null, null, null);

    assertEquals("a@b.com", user.getEmail());
    assertEquals("user", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("img", user.getImage());
  }

  @Test
  void should_update_all_fields_at_once() {
    User user = new User("a@b.com", "user", "pass", "bio", "img");
    user.update("new@b.com", "newuser", "newpass", "newbio", "newimg");

    assertEquals("new@b.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("newbio", user.getBio());
    assertEquals("newimg", user.getImage());
  }

  @Test
  void should_be_equal_when_same_id() {
    User user1 = new User("a@b.com", "user1", "pass", "", "");
    User user2 = new User("c@d.com", "user2", "pass", "", "");

    assertNotEquals(user1, user2);
    assertEquals(user1, user1);
  }
}
