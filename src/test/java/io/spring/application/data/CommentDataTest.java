package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.application.DateTimeCursor;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CommentDataTest {

  @Test
  public void should_create_with_all_args() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("user-id", "testuser", "bio", "image", false);

    CommentData data = new CommentData("id", "body", "article-id", now, now, profile);

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("article-id", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  public void should_create_with_no_args() {
    CommentData data = new CommentData();
    assertNotNull(data);
    assertNull(data.getId());
  }

  @Test
  public void should_get_cursor() {
    DateTime now = new DateTime();
    CommentData data = new CommentData("id", "body", "article-id", now, now, null);

    DateTimeCursor cursor = data.getCursor();
    assertNotNull(cursor);
    assertEquals(now.getMillis(), cursor.getData().getMillis());
  }

  @Test
  public void should_set_fields() {
    CommentData data = new CommentData();
    data.setId("new-id");
    data.setBody("new body");
    data.setArticleId("new-article-id");
    DateTime now = new DateTime();
    data.setCreatedAt(now);
    data.setUpdatedAt(now);
    data.setProfileData(new ProfileData("id", "user", "", "", false));

    assertEquals("new-id", data.getId());
    assertEquals("new body", data.getBody());
    assertEquals("new-article-id", data.getArticleId());
  }

  @Test
  public void should_have_equals_and_hashcode() {
    DateTime now = new DateTime();
    CommentData data1 = new CommentData("id", "body", "article-id", now, now, null);
    CommentData data2 = new CommentData("id", "body", "article-id", now, now, null);
    CommentData data3 = new CommentData("id2", "body", "article-id", now, now, null);

    assertEquals(data1, data2);
    assertNotEquals(data1, data3);
  }

  @Test
  public void should_have_to_string() {
    CommentData data = new CommentData();
    data.setId("test-id");
    String str = data.toString();
    assertNotNull(str);
    assertTrue(str.contains("test-id"));
  }
}
