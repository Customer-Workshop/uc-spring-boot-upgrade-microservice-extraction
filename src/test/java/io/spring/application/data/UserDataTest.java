package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserDataTest {

  @Test
  public void should_create_with_all_args_constructor() {
    UserData data = new UserData("id", "test@test.com", "testuser", "bio", "image");

    assertEquals("id", data.getId());
    assertEquals("test@test.com", data.getEmail());
    assertEquals("testuser", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
  }

  @Test
  public void should_create_with_no_args_and_setters() {
    UserData data = new UserData();
    data.setId("id");
    data.setEmail("test@test.com");
    data.setUsername("testuser");
    data.setBio("bio");
    data.setImage("image");

    assertEquals("id", data.getId());
    assertEquals("test@test.com", data.getEmail());
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    UserData data1 = new UserData("id", "email", "user", "bio", "image");
    UserData data2 = new UserData("id", "email", "user", "bio", "image");

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  public void should_not_equal_different_data() {
    UserData data1 = new UserData("id1", "email1", "user1", "bio", "image");
    UserData data2 = new UserData("id2", "email2", "user2", "bio", "image");

    assertNotEquals(data1, data2);
  }

  @Test
  public void should_implement_toString() {
    UserData data = new UserData("id", "email", "user", "bio", "image");

    String str = data.toString();

    assertNotNull(str);
    assertTrue(str.contains("user"));
  }
}
