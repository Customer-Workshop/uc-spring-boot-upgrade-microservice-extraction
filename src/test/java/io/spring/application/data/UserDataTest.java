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
  }

  @Test
  void should_set_and_get_all_fields() {
    UserData data = new UserData();
    data.setId("newId");
    data.setEmail("new@test.com");
    data.setUsername("newuser");
    data.setBio("new bio");
    data.setImage("new img");

    assertEquals("newId", data.getId());
    assertEquals("new@test.com", data.getEmail());
    assertEquals("newuser", data.getUsername());
    assertEquals("new bio", data.getBio());
    assertEquals("new img", data.getImage());
  }

  @Test
  void should_have_equals_and_hashcode() {
    UserData d1 = new UserData("id", "e", "u", "b", "i");
    UserData d2 = new UserData("id", "e", "u", "b", "i");
    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void should_have_toString() {
    UserData data = new UserData("id", "email", "user", "bio", "img");
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("email"));
  }
}
