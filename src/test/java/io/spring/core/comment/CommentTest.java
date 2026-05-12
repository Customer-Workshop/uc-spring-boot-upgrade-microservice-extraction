package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  void should_create_comment() {
    Comment comment = new Comment("This is a comment", "user-id", "article-id");

    assertNotNull(comment.getId());
    assertEquals("This is a comment", comment.getBody());
    assertEquals("user-id", comment.getUserId());
    assertEquals("article-id", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void should_have_unique_ids() {
    Comment c1 = new Comment("body1", "user1", "art1");
    Comment c2 = new Comment("body2", "user1", "art1");

    assertNotEquals(c1.getId(), c2.getId());
  }

  @Test
  void should_have_created_at_timestamp() {
    Comment comment = new Comment("body", "uid", "aid");
    assertNotNull(comment.getCreatedAt());
  }
}
