package io.spring.core.comment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_set_all_fields_in_constructor() {
    Comment comment = new Comment("content", "user-id", "article-id");
    assertThat(comment.getId(), notNullValue());
    assertThat(comment.getBody(), is("content"));
    assertThat(comment.getUserId(), is("user-id"));
    assertThat(comment.getArticleId(), is("article-id"));
    assertThat(comment.getCreatedAt(), notNullValue());
  }

  @Test
  public void should_generate_unique_id_for_each_comment() {
    Comment comment = new Comment("content", "user-id", "article-id");
    Comment another = new Comment("content", "user-id", "article-id");
    assertThat(comment.getId().equals(another.getId()), is(false));
    assertThat(comment.equals(another), is(false));
    assertThat(comment.equals(comment), is(true));
  }
}
