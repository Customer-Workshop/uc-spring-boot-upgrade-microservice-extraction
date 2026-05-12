package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ProfileDataTest {

  @Test
  public void should_create_with_all_args_constructor() {
    ProfileData data = new ProfileData("id", "username", "bio", "image", true);

    assertEquals("id", data.getId());
    assertEquals("username", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  public void should_create_with_no_args_and_setters() {
    ProfileData data = new ProfileData();
    data.setId("id");
    data.setUsername("user");
    data.setBio("bio");
    data.setImage("image");
    data.setFollowing(false);

    assertEquals("id", data.getId());
    assertEquals("user", data.getUsername());
    assertFalse(data.isFollowing());
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    ProfileData data1 = new ProfileData("id", "user", "bio", "image", false);
    ProfileData data2 = new ProfileData("id", "user", "bio", "image", false);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  public void should_not_equal_different_data() {
    ProfileData data1 = new ProfileData("id1", "user1", "bio", "image", false);
    ProfileData data2 = new ProfileData("id2", "user2", "bio", "image", false);

    assertNotEquals(data1, data2);
  }

  @Test
  public void should_implement_toString() {
    ProfileData data = new ProfileData("id", "user", "bio", "image", false);

    String str = data.toString();

    assertNotNull(str);
    assertTrue(str.contains("user"));
  }
}
