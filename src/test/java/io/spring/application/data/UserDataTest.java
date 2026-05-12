package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserDataTest {

  @Test
  void should_create_user_data_with_all_args() {
    UserData data = new UserData("id", "user@test.com", "username", "bio", "image.png");

    assertEquals("id", data.getId());
    assertEquals("user@test.com", data.getEmail());
    assertEquals("username", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image.png", data.getImage());
  }

  @Test
  void should_create_user_data_with_no_args() {
    UserData data = new UserData();

    assertNull(data.getId());
    assertNull(data.getEmail());
    assertNull(data.getUsername());
    assertNull(data.getBio());
    assertNull(data.getImage());
  }

  @Test
  void should_support_setters() {
    UserData data = new UserData();
    data.setId("new-id");
    data.setEmail("new@email.com");
    data.setUsername("newuser");
    data.setBio("new bio");
    data.setImage("new-img.png");

    assertEquals("new-id", data.getId());
    assertEquals("new@email.com", data.getEmail());
    assertEquals("newuser", data.getUsername());
    assertEquals("new bio", data.getBio());
    assertEquals("new-img.png", data.getImage());
  }

  @Test
  void should_support_equals_and_hashcode() {
    UserData d1 = new UserData("id", "e@e.com", "u", "b", "i");
    UserData d2 = new UserData("id", "e@e.com", "u", "b", "i");

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void should_not_equal_different_data() {
    UserData d1 = new UserData("id1", "e1@e.com", "u1", "b", "i");
    UserData d2 = new UserData("id2", "e2@e.com", "u2", "b", "i");

    assertNotEquals(d1, d2);
  }
}
