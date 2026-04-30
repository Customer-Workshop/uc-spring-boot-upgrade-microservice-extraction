package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment() {
    Comment comment = new Comment("body text", "user1", "article1");
    assertEquals("body text", comment.getBody());
    assertEquals("user1", comment.getUserId());
    assertEquals("article1", comment.getArticleId());
    assertNotNull(comment.getId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  public void should_have_unique_id() {
    Comment c1 = new Comment("body1", "user1", "article1");
    Comment c2 = new Comment("body2", "user1", "article1");
    assertNotEquals(c1.getId(), c2.getId());
  }

  @Test
  public void should_equal_by_id() {
    Comment c1 = new Comment("body", "user1", "article1");
    Comment c2 = new Comment("body", "user1", "article1");
    assertNotEquals(c1, c2);
    assertEquals(c1, c1);
  }
}
