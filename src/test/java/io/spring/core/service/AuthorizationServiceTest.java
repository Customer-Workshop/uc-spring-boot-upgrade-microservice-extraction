package io.spring.core.service;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  private User author;
  private User anotherUser;
  private Article article;

  @BeforeEach
  public void setUp() {
    author = new User("a@test.com", "a", "123", "", "");
    anotherUser = new User("b@test.com", "b", "123", "", "");
    article = new Article("title", "desc", "body", Arrays.asList("java"), author.getId());
  }

  @Test
  public void should_allow_author_to_write_article() {
    Assertions.assertTrue(AuthorizationService.canWriteArticle(author, article));
  }

  @Test
  public void should_not_allow_other_users_to_write_article() {
    Assertions.assertFalse(AuthorizationService.canWriteArticle(anotherUser, article));
  }

  @Test
  public void should_allow_comment_author_and_article_author_to_write_comment() {
    Comment comment = new Comment("content", anotherUser.getId(), article.getId());

    Assertions.assertTrue(AuthorizationService.canWriteComment(anotherUser, article, comment));
    Assertions.assertTrue(AuthorizationService.canWriteComment(author, article, comment));
  }

  @Test
  public void should_not_allow_unrelated_user_to_write_comment() {
    User thirdUser = new User("c@test.com", "c", "123", "", "");
    Comment comment = new Comment("content", anotherUser.getId(), article.getId());

    Assertions.assertFalse(AuthorizationService.canWriteComment(thirdUser, article, comment));
  }
}
