package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CommentDataTest {

  @Test
  void should_create_comment_data_with_all_args() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "author", "bio", "img", false);
    CommentData data = new CommentData("cid", "Comment body", "article-id", now, now, profile);

    assertEquals("cid", data.getId());
    assertEquals("Comment body", data.getBody());
    assertEquals("article-id", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void should_return_cursor_based_on_created_at() {
    DateTime createdAt = new DateTime(2023, 6, 1, 12, 0, 0);
    CommentData data = new CommentData();
    data.setCreatedAt(createdAt);

    DateTimeCursor cursor = data.getCursor();

    assertNotNull(cursor);
    assertEquals(new DateTimeCursor(createdAt).toString(), cursor.toString());
  }

  @Test
  void should_create_comment_data_with_no_args() {
    CommentData data = new CommentData();
    assertNull(data.getId());
    assertNull(data.getBody());
    assertNull(data.getArticleId());
  }

  @Test
  void should_support_equals_and_hashcode() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    CommentData d1 = new CommentData("id", "body", "art", now, now, profile);
    CommentData d2 = new CommentData("id", "body", "art", now, now, profile);

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void should_not_equal_different_data() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    CommentData d1 = new CommentData("id1", "body", "art", now, now, profile);
    CommentData d2 = new CommentData("id2", "body", "art", now, now, profile);

    assertNotEquals(d1, d2);
  }
}
