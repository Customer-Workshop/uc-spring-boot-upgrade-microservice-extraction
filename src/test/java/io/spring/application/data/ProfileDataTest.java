package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ProfileDataTest {

  @Test
  public void should_create_profile_data() {
    ProfileData profile = new ProfileData("id", "username", "bio", "image", true);
    assertEquals("id", profile.getId());
    assertEquals("username", profile.getUsername());
    assertEquals("bio", profile.getBio());
    assertEquals("image", profile.getImage());
    assertTrue(profile.isFollowing());
  }

  @Test
  public void should_create_profile_not_following() {
    ProfileData profile = new ProfileData("id", "username", "bio", "image", false);
    assertFalse(profile.isFollowing());
  }

  @Test
  public void should_set_following() {
    ProfileData profile = new ProfileData("id", "username", "bio", "image", false);
    profile.setFollowing(true);
    assertTrue(profile.isFollowing());
  }

  @Test
  public void should_create_empty_profile() {
    ProfileData profile = new ProfileData();
    assertNull(profile.getId());
    assertNull(profile.getUsername());
    assertNull(profile.getBio());
    assertNull(profile.getImage());
    assertFalse(profile.isFollowing());
  }

  @Test
  public void should_set_all_fields() {
    ProfileData profile = new ProfileData();
    profile.setId("newId");
    profile.setUsername("newUser");
    profile.setBio("newBio");
    profile.setImage("newImage");
    profile.setFollowing(true);
    assertEquals("newId", profile.getId());
    assertEquals("newUser", profile.getUsername());
    assertEquals("newBio", profile.getBio());
    assertEquals("newImage", profile.getImage());
    assertTrue(profile.isFollowing());
  }

  @Test
  public void should_implement_equals() {
    ProfileData p1 = new ProfileData("id", "username", "bio", "image", true);
    ProfileData p2 = new ProfileData("id", "username", "bio", "image", true);
    assertEquals(p1, p2);
  }

  @Test
  public void should_not_equal_different_profile() {
    ProfileData p1 = new ProfileData("id1", "user1", "bio1", "img1", true);
    ProfileData p2 = new ProfileData("id2", "user2", "bio2", "img2", false);
    assertNotEquals(p1, p2);
  }

  @Test
  public void should_implement_hashcode() {
    ProfileData p1 = new ProfileData("id", "username", "bio", "image", true);
    ProfileData p2 = new ProfileData("id", "username", "bio", "image", true);
    assertEquals(p1.hashCode(), p2.hashCode());
  }

  @Test
  public void should_implement_tostring() {
    ProfileData profile = new ProfileData("id", "username", "bio", "image", true);
    String str = profile.toString();
    assertNotNull(str);
    assertTrue(str.contains("username"));
  }

  @Test
  public void should_not_equal_null() {
    ProfileData profile = new ProfileData("id", "username", "bio", "image", true);
    assertNotEquals(null, profile);
  }

  @Test
  public void should_equal_self() {
    ProfileData profile = new ProfileData("id", "username", "bio", "image", true);
    assertEquals(profile, profile);
  }
}
