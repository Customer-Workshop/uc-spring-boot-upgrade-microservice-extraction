package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserDataTest {

  @Test
  void should_create_with_all_args() {
    UserData data = new UserData("id", "email@test.com", "username", "bio", "image");

    assertEquals("id", data.getId());
    assertEquals("email@test.com", data.getEmail());
    assertEquals("username", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
  }

  @Test
  void should_create_with_no_args() {
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
    data.setId("newId");
    data.setEmail("new@test.com");
    data.setUsername("newUser");
    data.setBio("new bio");
    data.setImage("new.jpg");

    assertEquals("newId", data.getId());
    assertEquals("new@test.com", data.getEmail());
    assertEquals("newUser", data.getUsername());
    assertEquals("new bio", data.getBio());
    assertEquals("new.jpg", data.getImage());
  }

  @Test
  void should_implement_equals_and_hashcode() {
    UserData data1 = new UserData("id", "email@test.com", "user", "bio", "img");
    UserData data2 = new UserData("id", "email@test.com", "user", "bio", "img");

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_not_equal_different_data() {
    UserData data1 = new UserData("id1", "email1@test.com", "user1", "bio1", "img1");
    UserData data2 = new UserData("id2", "email2@test.com", "user2", "bio2", "img2");

    assertNotEquals(data1, data2);
  }

  @Test
  void should_implement_toString() {
    UserData data = new UserData("id", "email@test.com", "user", "bio", "img");
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("user"));
  }
}
