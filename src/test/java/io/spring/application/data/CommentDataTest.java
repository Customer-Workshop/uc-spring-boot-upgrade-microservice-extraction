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
    ProfileData profile = new ProfileData("uid", "user", "", "", false);
    CommentData data = new CommentData("id", "body", "aid", now, now, profile);

    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
    assertEquals(now, cursor.getData());
  }

  @Test
  void should_support_setters() {
    CommentData data = new CommentData();
    data.setId("newId");
    data.setBody("new body");
    data.setArticleId("newAid");
    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    ProfileData profile = new ProfileData("uid", "user", "", "", false);
    data.setProfileData(profile);

    assertEquals("newId", data.getId());
    assertEquals("new body", data.getBody());
    assertEquals("newAid", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_implement_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "", "", false);
    CommentData data1 = new CommentData("id", "body", "aid", now, now, profile);
    CommentData data2 = new CommentData("id", "body", "aid", now, now, profile);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  void should_implement_toString() {
    CommentData data = new CommentData();
    data.setId("test-id");
    assertNotNull(data.toString());
  }
}
