package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CommentDataTest {

  @Test
  public void should_create_comment_data() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id", "username", "bio", "image", false);
    CommentData comment = new CommentData("commentId", "body", "articleId", now, now, profile);
    assertEquals("commentId", comment.getId());
    assertEquals("body", comment.getBody());
    assertEquals("articleId", comment.getArticleId());
    assertEquals(now, comment.getCreatedAt());
    assertEquals(now, comment.getUpdatedAt());
    assertEquals(profile, comment.getProfileData());
  }

  @Test
  public void should_return_cursor_from_created_at() {
    DateTime now = new DateTime();
    CommentData comment = new CommentData("id", "body", "articleId", now, now, null);
    assertNotNull(comment.getCursor());
    assertEquals(now, comment.getCursor().getData());
  }

  @Test
  public void should_create_empty_comment_data() {
    CommentData comment = new CommentData();
    assertNull(comment.getId());
    assertNull(comment.getBody());
  }

  @Test
  public void should_set_all_fields() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    CommentData comment = new CommentData();
    comment.setId("id");
    comment.setBody("body");
    comment.setArticleId("articleId");
    comment.setCreatedAt(now);
    comment.setUpdatedAt(now);
    comment.setProfileData(profile);
    assertEquals("id", comment.getId());
    assertEquals("body", comment.getBody());
    assertEquals("articleId", comment.getArticleId());
    assertEquals(now, comment.getCreatedAt());
    assertEquals(now, comment.getUpdatedAt());
    assertEquals(profile, comment.getProfileData());
  }

  @Test
  public void should_implement_equals() {
    DateTime now = new DateTime();
    CommentData c1 = new CommentData("id", "body", "art", now, now, null);
    CommentData c2 = new CommentData("id", "body", "art", now, now, null);
    assertEquals(c1, c2);
  }

  @Test
  public void should_not_equal_different_comment() {
    DateTime now = new DateTime();
    CommentData c1 = new CommentData("id1", "body1", "art1", now, now, null);
    CommentData c2 = new CommentData("id2", "body2", "art2", now, now, null);
    assertNotEquals(c1, c2);
  }

  @Test
  public void should_implement_hashcode() {
    DateTime now = new DateTime();
    CommentData c1 = new CommentData("id", "body", "art", now, now, null);
    CommentData c2 = new CommentData("id", "body", "art", now, now, null);
    assertEquals(c1.hashCode(), c2.hashCode());
  }

  @Test
  public void should_implement_tostring() {
    CommentData comment = new CommentData();
    comment.setBody("test body");
    String str = comment.toString();
    assertNotNull(str);
    assertTrue(str.contains("test body"));
  }

  @Test
  public void should_equal_self() {
    CommentData comment = new CommentData();
    assertEquals(comment, comment);
  }

  @Test
  public void should_not_equal_null() {
    CommentData comment = new CommentData();
    assertNotEquals(null, comment);
  }
}
