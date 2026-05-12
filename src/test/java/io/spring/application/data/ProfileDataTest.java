package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ProfileDataTest {

  @Test
  void should_create_profile_data_with_all_args() {
    ProfileData data = new ProfileData("id", "username", "bio", "image.png", true);

    assertEquals("id", data.getId());
    assertEquals("username", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image.png", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void should_create_profile_data_not_following() {
    ProfileData data = new ProfileData("id", "user", "bio", "img", false);

    assertFalse(data.isFollowing());
  }

  @Test
  void should_create_profile_data_with_no_args() {
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
    data.setId("new-id");
    data.setUsername("newuser");
    data.setBio("new bio");
    data.setImage("new-img.png");
    data.setFollowing(true);

    assertEquals("new-id", data.getId());
    assertEquals("newuser", data.getUsername());
    assertEquals("new bio", data.getBio());
    assertEquals("new-img.png", data.getImage());
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
    ProfileData d1 = new ProfileData("id1", "user1", "bio", "img", false);
    ProfileData d2 = new ProfileData("id2", "user2", "bio", "img", false);

    assertNotEquals(d1, d2);
  }
}
