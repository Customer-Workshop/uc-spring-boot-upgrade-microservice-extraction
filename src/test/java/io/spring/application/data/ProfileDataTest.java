package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ProfileDataTest {

  @Test
  void should_create_with_all_args() {
    ProfileData data = new ProfileData("id", "user", "bio", "img", true);
    assertEquals("id", data.getId());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void should_create_with_no_args() {
    ProfileData data = new ProfileData();
    assertNull(data.getId());
    assertNull(data.getUsername());
    assertFalse(data.isFollowing());
  }

  @Test
  void should_support_setters() {
    ProfileData data = new ProfileData();
    data.setId("id");
    data.setUsername("user");
    data.setBio("bio");
    data.setImage("img");
    data.setFollowing(true);

    assertEquals("id", data.getId());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("img", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void should_support_equals_and_hashcode() {
    ProfileData d1 = new ProfileData("id", "user", "bio", "img", false);
    ProfileData d2 = new ProfileData("id", "user", "bio", "img", false);
    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void should_not_equal_different_data() {
    ProfileData d1 = new ProfileData("id1", "user1", "bio1", "img1", false);
    ProfileData d2 = new ProfileData("id2", "user2", "bio2", "img2", true);
    assertNotEquals(d1, d2);
  }

  @Test
  void should_support_to_string() {
    ProfileData data = new ProfileData("id", "user", "bio", "img", false);
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("user"));
  }
}
