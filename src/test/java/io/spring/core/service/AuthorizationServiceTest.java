package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  private User author;
  private User otherUser;
  private Article article;

  @BeforeEach
  void setUp() {
    author = new User("author@test.com", "author", "pass", "bio", "image");
    otherUser = new User("other@test.com", "other", "pass", "bio", "image");
    article = new Article("Test", "desc", "body", Arrays.asList("java"), author.getId());
  }

  @Test
  void should_allow_author_to_write_article() {
    assertTrue(AuthorizationService.canWriteArticle(author, article));
  }

  @Test
  void should_deny_non_author_to_write_article() {
    assertFalse(AuthorizationService.canWriteArticle(otherUser, article));
  }

  @Test
  void should_allow_article_owner_to_write_comment() {
    Comment comment = new Comment("body", otherUser.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(author, article, comment));
  }

  @Test
  void should_allow_comment_owner_to_write_comment() {
    Comment comment = new Comment("body", otherUser.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(otherUser, article, comment));
  }

  @Test
  void should_deny_unrelated_user_to_write_comment() {
    User thirdUser = new User("third@test.com", "third", "pass", "", "");
    Comment comment = new Comment("body", otherUser.getId(), article.getId());
    assertFalse(AuthorizationService.canWriteComment(thirdUser, article, comment));
  }
}
