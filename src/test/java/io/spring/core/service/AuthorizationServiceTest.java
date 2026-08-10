package io.spring.core.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  private User newUser() {
    return new User("john@example.com", "john", "123", "bio", "image");
  }

  private Article newArticle(String userId) {
    return new Article("title", "desc", "body", Arrays.asList("java"), userId);
  }

  @Test
  public void should_allow_author_to_write_article() {
    User user = newUser();
    assertThat(AuthorizationService.canWriteArticle(user, newArticle(user.getId())), is(true));
  }

  @Test
  public void should_not_allow_other_user_to_write_article() {
    User user = newUser();
    assertThat(AuthorizationService.canWriteArticle(user, newArticle("other-user")), is(false));
  }

  @Test
  public void should_allow_article_author_to_write_comment() {
    User user = newUser();
    Article article = newArticle(user.getId());
    Comment comment = new Comment("content", "other-user", article.getId());
    assertThat(AuthorizationService.canWriteComment(user, article, comment), is(true));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    User user = newUser();
    Article article = newArticle("other-user");
    Comment comment = new Comment("content", user.getId(), article.getId());
    assertThat(AuthorizationService.canWriteComment(user, article, comment), is(true));
  }

  @Test
  public void should_not_allow_other_user_to_write_comment() {
    User user = newUser();
    Article article = newArticle("other-user");
    Comment comment = new Comment("content", "another-user", article.getId());
    assertThat(AuthorizationService.canWriteComment(user, article, comment), is(false));
  }
}
