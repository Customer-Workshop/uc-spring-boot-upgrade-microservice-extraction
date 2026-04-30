package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ProfileDataTest {

  @Test
  public void should_create_with_all_args() {
    ProfileData data = new ProfileData("id", "testuser", "bio", "image", true);

    assertEquals("id", data.getId());
    assertEquals("testuser", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  public void should_create_with_no_args() {
    ProfileData data = new ProfileData();
    assertNotNull(data);
    assertNull(data.getId());
    assertFalse(data.isFollowing());
  }

  @Test
  public void should_set_fields() {
    ProfileData data = new ProfileData();
    data.setId("new-id");
    data.setUsername("newuser");
    data.setBio("new bio");
    data.setImage("new image");
    data.setFollowing(true);

    assertEquals("new-id", data.getId());
    assertEquals("newuser", data.getUsername());
    assertEquals("new bio", data.getBio());
    assertEquals("new image", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  public void should_have_equals_and_hashcode() {
    ProfileData data1 = new ProfileData("id", "user", "bio", "image", false);
    ProfileData data2 = new ProfileData("id", "user", "bio", "image", false);
    ProfileData data3 = new ProfileData("id2", "user2", "bio", "image", false);

    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  public void should_have_to_string() {
    ProfileData data = new ProfileData("id", "testuser", "bio", "image", false);
    String str = data.toString();
    assertNotNull(str);
    assertTrue(str.contains("testuser"));
  }
}
