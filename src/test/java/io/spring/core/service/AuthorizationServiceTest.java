package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  void should_allow_article_author_to_write() {
    User user = new User("test@test.com", "user", "pass", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());

    assertTrue(AuthorizationService.canWriteArticle(user, article));
  }

  @Test
  void should_deny_non_author_to_write_article() {
    User author = new User("author@test.com", "author", "pass", "", "");
    User other = new User("other@test.com", "other", "pass", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), author.getId());

    assertFalse(AuthorizationService.canWriteArticle(other, article));
  }

  @Test
  void should_allow_article_owner_to_write_comment() {
    User articleOwner = new User("owner@test.com", "owner", "pass", "", "");
    User commenter = new User("commenter@test.com", "commenter", "pass", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), articleOwner.getId());
    Comment comment = new Comment("body", commenter.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(articleOwner, article, comment));
  }

  @Test
  void should_allow_comment_author_to_write_comment() {
    User articleOwner = new User("owner@test.com", "owner", "pass", "", "");
    User commenter = new User("commenter@test.com", "commenter", "pass", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), articleOwner.getId());
    Comment comment = new Comment("body", commenter.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(commenter, article, comment));
  }

  @Test
  void should_deny_unrelated_user_to_write_comment() {
    User articleOwner = new User("owner@test.com", "owner", "pass", "", "");
    User commenter = new User("commenter@test.com", "commenter", "pass", "", "");
    User unrelated = new User("unrelated@test.com", "unrelated", "pass", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), articleOwner.getId());
    Comment comment = new Comment("body", commenter.getId(), article.getId());

    assertFalse(AuthorizationService.canWriteComment(unrelated, article, comment));
  }
}
