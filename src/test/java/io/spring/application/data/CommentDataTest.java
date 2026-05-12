package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CommentDataTest {

  @Test
  void should_create_with_all_args() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    CommentData data = new CommentData("id", "body", "articleId", now, now, profile);

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("articleId", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_create_with_no_args() {
    CommentData data = new CommentData();
    assertNull(data.getId());
    assertNull(data.getBody());
    assertNull(data.getArticleId());
  }

  @Test
  void should_get_cursor_based_on_createdAt() {
    DateTime now = new DateTime();
    CommentData data = new CommentData();
    data.setCreatedAt(now);
    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
    assertEquals(now, cursor.getData());
  }

  @Test
  void should_set_and_get_all_fields() {
    CommentData data = new CommentData();
    DateTime now = new DateTime();
    data.setId("cid");
    data.setBody("comment body");
    data.setArticleId("aid");
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setProfileData(new ProfileData("pid", "user", "", "", true));

    assertEquals("cid", data.getId());
    assertEquals("comment body", data.getBody());
    assertEquals("aid", data.getArticleId());
    assertTrue(data.getProfileData().isFollowing());
  }

  @Test
  void should_have_equals_and_hashcode() {
    DateTime now = new DateTime();
    CommentData d1 = new CommentData("id", "body", "aid", now, now, null);
    CommentData d2 = new CommentData("id", "body", "aid", now, now, null);

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void should_have_toString() {
    CommentData data = new CommentData();
    data.setId("testId");
    assertNotNull(data.toString());
    assertTrue(data.toString().contains("testId"));
  }
}
