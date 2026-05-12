package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment() {
    Comment comment = new Comment("body", "user-id", "article-id");

    assertNotNull(comment.getId());
    assertEquals("body", comment.getBody());
    assertEquals("user-id", comment.getUserId());
    assertEquals("article-id", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  public void should_create_with_no_args_constructor() {
    Comment comment = new Comment();

    assertNull(comment.getId());
    assertNull(comment.getBody());
  }

  @Test
  public void should_implement_equals_by_id() {
    Comment c1 = new Comment("body", "user-id", "article-id");
    Comment c2 = new Comment("body", "user-id", "article-id");

    assertNotEquals(c1, c2);
    assertEquals(c1, c1);
  }

  @Test
  public void should_implement_hashcode_by_id() {
    Comment comment = new Comment("body", "user-id", "article-id");

    assertEquals(comment.hashCode(), comment.hashCode());
  }
}
