package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UserDataTest {

  @Test
  public void should_create_user_data() {
    UserData userData = new UserData("id", "email@test.com", "username", "bio", "image");
    assertEquals("id", userData.getId());
    assertEquals("email@test.com", userData.getEmail());
    assertEquals("username", userData.getUsername());
    assertEquals("bio", userData.getBio());
    assertEquals("image", userData.getImage());
  }

  @Test
  public void should_create_empty_user_data() {
    UserData userData = new UserData();
    assertNull(userData.getId());
    assertNull(userData.getEmail());
    assertNull(userData.getUsername());
    assertNull(userData.getBio());
    assertNull(userData.getImage());
  }

  @Test
  public void should_set_fields() {
    UserData userData = new UserData();
    userData.setId("id");
    userData.setEmail("email@test.com");
    userData.setUsername("username");
    userData.setBio("bio");
    userData.setImage("image");
    assertEquals("id", userData.getId());
    assertEquals("email@test.com", userData.getEmail());
    assertEquals("username", userData.getUsername());
    assertEquals("bio", userData.getBio());
    assertEquals("image", userData.getImage());
  }

  @Test
  public void should_implement_equals() {
    UserData u1 = new UserData("id", "email", "user", "bio", "img");
    UserData u2 = new UserData("id", "email", "user", "bio", "img");
    assertEquals(u1, u2);
  }

  @Test
  public void should_not_equal_different_user() {
    UserData u1 = new UserData("id1", "email1", "user1", "bio1", "img1");
    UserData u2 = new UserData("id2", "email2", "user2", "bio2", "img2");
    assertNotEquals(u1, u2);
  }

  @Test
  public void should_implement_hashcode() {
    UserData u1 = new UserData("id", "email", "user", "bio", "img");
    UserData u2 = new UserData("id", "email", "user", "bio", "img");
    assertEquals(u1.hashCode(), u2.hashCode());
  }

  @Test
  public void should_implement_tostring() {
    UserData userData = new UserData("id", "email", "user", "bio", "img");
    String str = userData.toString();
    assertNotNull(str);
    assertTrue(str.contains("user"));
  }

  @Test
  public void should_equal_self() {
    UserData userData = new UserData("id", "email", "user", "bio", "img");
    assertEquals(userData, userData);
  }

  @Test
  public void should_not_equal_null() {
    UserData userData = new UserData("id", "email", "user", "bio", "img");
    assertNotEquals(null, userData);
  }
}
