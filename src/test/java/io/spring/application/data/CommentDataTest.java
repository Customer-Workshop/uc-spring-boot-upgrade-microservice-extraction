package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CommentDataTest {

  @Test
  public void should_create_with_all_args_constructor() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id", "user", "bio", "image", false);
    CommentData data = new CommentData("id", "body", "article-id", now, now, profile);

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("article-id", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  public void should_create_with_no_args_and_setters() {
    CommentData data = new CommentData();
    DateTime now = new DateTime();
    data.setId("id");
    data.setBody("body");
    data.setArticleId("article-id");
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setProfileData(null);

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
    assertNull(data.getProfileData());
  }

  @Test
  public void should_return_cursor_from_created_at() {
    DateTime now = new DateTime();
    CommentData data = new CommentData("id", "body", "article-id", now, now, null);

    DateTimeCursor cursor = data.getCursor();

    assertNotNull(cursor);
    assertEquals(now, cursor.getData());
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    DateTime now = new DateTime();
    CommentData data1 = new CommentData("id", "body", "article-id", now, now, null);
    CommentData data2 = new CommentData("id", "body", "article-id", now, now, null);

    assertEquals(data1, data2);
    assertEquals(data1.hashCode(), data2.hashCode());
  }

  @Test
  public void should_not_equal_different_data() {
    DateTime now = new DateTime();
    CommentData data1 = new CommentData("id1", "body", "article-id", now, now, null);
    CommentData data2 = new CommentData("id2", "body", "article-id", now, now, null);

    assertNotEquals(data1, data2);
  }

  @Test
  public void should_implement_toString() {
    DateTime now = new DateTime();
    CommentData data = new CommentData("id", "body", "article-id", now, now, null);

    String str = data.toString();

    assertNotNull(str);
    assertTrue(str.contains("id"));
  }
}
