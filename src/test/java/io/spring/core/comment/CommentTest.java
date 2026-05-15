package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment() {
    Comment comment = new Comment("comment body", "userId", "articleId");
    assertEquals("comment body", comment.getBody());
    assertEquals("userId", comment.getUserId());
    assertEquals("articleId", comment.getArticleId());
    assertNotNull(comment.getId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  public void should_generate_unique_id() {
    Comment c1 = new Comment("body1", "user1", "article1");
    Comment c2 = new Comment("body2", "user2", "article2");
    assertNotEquals(c1.getId(), c2.getId());
  }

  @Test
  public void should_use_id_for_equality() {
    Comment c1 = new Comment("body", "user", "article");
    Comment c2 = new Comment("body", "user", "article");
    assertNotEquals(c1, c2);
    assertEquals(c1, c1);
  }

  @Test
  public void should_create_empty_comment_with_no_args() {
    Comment comment = new Comment();
    assertNull(comment.getId());
    assertNull(comment.getBody());
    assertNull(comment.getUserId());
    assertNull(comment.getArticleId());
    assertNull(comment.getCreatedAt());
  }
}
