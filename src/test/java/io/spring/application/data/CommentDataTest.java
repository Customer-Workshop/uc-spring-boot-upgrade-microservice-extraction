package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CommentDataTest {

  @Test
  public void should_create_comment_data() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    CommentData cd = new CommentData("cid", "body", "aid", now, now, profile);

    assertEquals("cid", cd.getId());
    assertEquals("body", cd.getBody());
    assertEquals("aid", cd.getArticleId());
    assertEquals(now, cd.getCreatedAt());
    assertEquals(now, cd.getUpdatedAt());
    assertEquals(profile, cd.getProfileData());
  }

  @Test
  public void should_get_cursor_based_on_created_at() {
    DateTime now = new DateTime();
    CommentData cd = new CommentData("cid", "body", "aid", now, now, null);
    DateTimeCursor cursor = cd.getCursor();

    assertNotNull(cursor);
    assertEquals(now, cursor.getData());
  }
}
