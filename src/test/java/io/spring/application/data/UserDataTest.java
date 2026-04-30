package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UserDataTest {

  @Test
  public void should_create_with_all_args() {
    UserData data = new UserData("id", "test@test.com", "testuser", "bio", "image");

    assertEquals("id", data.getId());
    assertEquals("test@test.com", data.getEmail());
    assertEquals("testuser", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
  }

  @Test
  public void should_create_with_no_args() {
    UserData data = new UserData();
    assertNotNull(data);
    assertNull(data.getId());
  }

  @Test
  public void should_set_fields() {
    UserData data = new UserData();
    data.setId("new-id");
    data.setEmail("new@test.com");
    data.setUsername("newuser");
    data.setBio("new bio");
    data.setImage("new image");

    assertEquals("new-id", data.getId());
    assertEquals("new@test.com", data.getEmail());
    assertEquals("newuser", data.getUsername());
    assertEquals("new bio", data.getBio());
    assertEquals("new image", data.getImage());
  }

  @Test
  public void should_have_equals_and_hashcode() {
    UserData data1 = new UserData("id", "e@e.com", "user", "bio", "img");
    UserData data2 = new UserData("id", "e@e.com", "user", "bio", "img");
    UserData data3 = new UserData("id2", "e@e.com", "user2", "bio", "img");

    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  public void should_have_to_string() {
    UserData data = new UserData("id", "e@e.com", "testuser", "bio", "img");
    String str = data.toString();
    assertNotNull(str);
    assertTrue(str.contains("testuser"));
  }
}
