package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserDataTest {

  @Test
  void should_create_with_all_args() {
    UserData data = new UserData("id", "email@test.com", "user", "bio", "img");
    assertEquals("id", data.getId());
    assertEquals("email@test.com", data.getEmail());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
  }

  @Test
  void should_create_with_no_args() {
    UserData data = new UserData();
    assertNull(data.getId());
    assertNull(data.getEmail());
    assertNull(data.getUsername());
  }

  @Test
  void should_support_setters() {
    UserData data = new UserData();
    data.setId("id");
    data.setEmail("e@t.com");
    data.setUsername("user");
    data.setBio("bio");
    data.setImage("img");

    assertEquals("id", data.getId());
    assertEquals("e@t.com", data.getEmail());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
  }

  @Test
  void should_support_equals_and_hashcode() {
    UserData d1 = new UserData("id", "e@t.com", "user", "bio", "img");
    UserData d2 = new UserData("id", "e@t.com", "user", "bio", "img");
    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void should_not_equal_different_data() {
    UserData d1 = new UserData("id1", "a@t.com", "user1", "bio1", "img1");
    UserData d2 = new UserData("id2", "b@t.com", "user2", "bio2", "img2");
    assertNotEquals(d1, d2);
  }

  @Test
  void should_support_to_string() {
    UserData data = new UserData("id", "e@t.com", "user", "bio", "img");
    assertNotNull(data.toString());
  }
}
