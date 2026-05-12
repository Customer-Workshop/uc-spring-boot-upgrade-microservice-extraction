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
  void should_get_cursor_from_created_at() {
    DateTime now = new DateTime();
    CommentData data = new CommentData();
    data.setCreatedAt(now);
    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
  }

  @Test
  void should_support_setters() {
    CommentData data = new CommentData();
    data.setId("id");
    data.setBody("body");
    data.setArticleId("aid");
    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setProfileData(new ProfileData());

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("aid", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertNotNull(data.getProfileData());
  }

  @Test
  void should_support_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    CommentData d1 = new CommentData("id", "body", "aid", now, now, profile);
    CommentData d2 = new CommentData("id", "body", "aid", now, now, profile);
    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void should_support_to_string() {
    CommentData data = new CommentData();
    data.setId("test");
    assertNotNull(data.toString());
  }
}
