package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  @Test
  void should_allow_article_author_to_write_article() {
    User author = new User("author@test.com", "author", "pass", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), author.getId());

    assertTrue(AuthorizationService.canWriteArticle(author, article));
  }

  @Test
  void should_not_allow_non_author_to_write_article() {
    User author = new User("author@test.com", "author", "pass", "", "");
    User other = new User("other@test.com", "other", "pass", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), author.getId());

    assertFalse(AuthorizationService.canWriteArticle(other, article));
  }

  @Test
  void should_allow_article_author_to_write_comment() {
    User articleAuthor = new User("author@test.com", "author", "pass", "", "");
    User commentAuthor = new User("commenter@test.com", "commenter", "pass", "", "");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("comment body", commentAuthor.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(articleAuthor, article, comment));
  }

  @Test
  void should_allow_comment_author_to_write_comment() {
    User articleAuthor = new User("author@test.com", "author", "pass", "", "");
    User commentAuthor = new User("commenter@test.com", "commenter", "pass", "", "");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("comment body", commentAuthor.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(commentAuthor, article, comment));
  }

  @Test
  void should_not_allow_random_user_to_write_comment() {
    User articleAuthor = new User("author@test.com", "author", "pass", "", "");
    User commentAuthor = new User("commenter@test.com", "commenter", "pass", "", "");
    User randomUser = new User("random@test.com", "random", "pass", "", "");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
    Comment comment = new Comment("comment body", commentAuthor.getId(), article.getId());

    assertFalse(AuthorizationService.canWriteComment(randomUser, article, comment));
  }
}
