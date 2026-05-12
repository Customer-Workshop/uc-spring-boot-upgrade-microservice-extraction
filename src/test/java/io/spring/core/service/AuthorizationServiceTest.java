package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  public void should_allow_author_to_write_article() {
    User user = new User("a@b.com", "user", "pass", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    assertTrue(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  public void should_deny_non_author_to_write_article() {
    User user = new User("a@b.com", "user", "pass", "", "");
    User other = new User("b@c.com", "other", "pass", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), other.getId());
    assertFalse(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  public void should_allow_article_author_to_write_comment() {
    User user = new User("a@b.com", "user", "pass", "", "");
    User commenter = new User("b@c.com", "commenter", "pass", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    Comment comment = new Comment("content", commenter.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(user, article, comment));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    User user = new User("a@b.com", "user", "pass", "", "");
    User other = new User("b@c.com", "other", "pass", "", "");
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), other.getId());
    Comment comment = new Comment("content", user.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(user, article, comment));
  }

  @Test
  public void should_deny_unrelated_user_to_write_comment() {
    User user = new User("a@b.com", "user", "pass", "", "");
    User articleAuthor = new User("b@c.com", "author", "pass", "", "");
    User commenter = new User("c@d.com", "commenter", "pass", "", "");
    Article article =
        new Article("title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("content", commenter.getId(), article.getId());
    assertFalse(AuthorizationService.canWriteComment(user, article, comment));
  }
}
