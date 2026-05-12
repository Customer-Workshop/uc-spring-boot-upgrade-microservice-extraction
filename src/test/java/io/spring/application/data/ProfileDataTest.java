package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ProfileDataTest {

  @Test
  void should_create_with_all_args() {
    ProfileData data = new ProfileData("id", "username", "bio", "image", true);

    assertEquals("id", data.getId());
    assertEquals("username", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void should_create_with_no_args() {
    ProfileData data = new ProfileData();

    assertNull(data.getId());
    assertNull(data.getUsername());
    assertNull(data.getBio());
    assertNull(data.getImage());
    assertFalse(data.isFollowing());
  }

  @Test
  void should_support_setters() {
    ProfileData data = new ProfileData();
    data.setId("newId");
    data.setUsername("newUser");
    data.setBio("new bio");
    data.setImage("new.jpg");
    data.setFollowing(true);

    assertEquals("newId", data.getId());
    assertEquals("newUser", data.getUsername());
    assertEquals("new bio", data.getBio());
    assertEquals("new.jpg", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void should_implement_equals_and_hashcode() {
    ProfileData data1 = new ProfileData("id", "user", "bio", "img", false);
    ProfileData data2 = new ProfileData("id", "user", "bio", "img", false);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_not_equal_different_data() {
    ProfileData data1 = new ProfileData("id1", "user1", "bio1", "img1", false);
    ProfileData data2 = new ProfileData("id2", "user2", "bio2", "img2", true);

    assertNotEquals(data1, data2);
  }

  @Test
  void should_implement_toString() {
    ProfileData data = new ProfileData("id", "user", "bio", "img", false);
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("user"));
  }
}
