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
    User user = new User("test@test.com", "testuser", "123", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    assertTrue(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  public void should_deny_non_author_to_write_article() {
    User user = new User("test@test.com", "testuser", "123", "", "");
    User other = new User("other@test.com", "other", "123", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), other.getId());
    assertFalse(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  public void should_allow_article_owner_to_write_comment() {
    User articleOwner = new User("owner@test.com", "owner", "123", "", "");
    User commenter = new User("commenter@test.com", "commenter", "123", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), articleOwner.getId());
    Comment comment = new Comment("body", commenter.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(articleOwner, article, comment));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    User articleOwner = new User("owner@test.com", "owner", "123", "", "");
    User commenter = new User("commenter@test.com", "commenter", "123", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), articleOwner.getId());
    Comment comment = new Comment("body", commenter.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(commenter, article, comment));
  }

  @Test
  public void should_deny_random_user_to_write_comment() {
    User articleOwner = new User("owner@test.com", "owner", "123", "", "");
    User commenter = new User("commenter@test.com", "commenter", "123", "", "");
    User random = new User("random@test.com", "random", "123", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), articleOwner.getId());
    Comment comment = new Comment("body", commenter.getId(), article.getId());
    assertFalse(AuthorizationService.canWriteComment(random, article, comment));
  }
}
