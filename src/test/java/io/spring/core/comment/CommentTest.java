package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  void should_create_comment_with_all_fields() {
    Comment comment = new Comment("body text", "user123", "article456");

    assertNotNull(comment.getId());
    assertEquals("body text", comment.getBody());
    assertEquals("user123", comment.getUserId());
    assertEquals("article456", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void should_generate_unique_ids() {
    Comment comment1 = new Comment("body1", "user1", "article1");
    Comment comment2 = new Comment("body2", "user2", "article2");

    assertNotEquals(comment1.getId(), comment2.getId());
  }

  @Test
  void should_be_equal_when_same_id() {
    Comment comment = new Comment("body", "user1", "article1");

    assertEquals(comment, comment);
  }

  @Test
  void should_not_be_equal_when_different_id() {
    Comment comment1 = new Comment("body", "user1", "article1");
    Comment comment2 = new Comment("body", "user1", "article1");

    assertNotEquals(comment1, comment2);
  }
}
