package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.Util;
import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  private User author;
  private User other;
  private Article article;

  @BeforeEach
  public void setUp() {
    author = new User("author@test.com", "author", "123", "", "");
    other = new User("other@test.com", "other", "123", "", "");
    article = new Article("title", "desc", "body", Arrays.asList("java"), author.getId());
  }

  @Test
  public void should_allow_author_to_write_article() {
    assertTrue(AuthorizationService.canWriteArticle(author, article));
  }

  @Test
  public void should_not_allow_other_user_to_write_article() {
    assertFalse(AuthorizationService.canWriteArticle(other, article));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    Comment comment = new Comment("content", other.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(other, article, comment));
  }

  @Test
  public void should_allow_article_author_to_write_comment_of_other_user() {
    Comment comment = new Comment("content", other.getId(), article.getId());

    assertTrue(AuthorizationService.canWriteComment(author, article, comment));
  }

  @Test
  public void should_not_allow_unrelated_user_to_write_comment() {
    User stranger = new User("stranger@test.com", "stranger", "123", "", "");
    Comment comment = new Comment("content", other.getId(), article.getId());

    assertFalse(AuthorizationService.canWriteComment(stranger, article, comment));
  }

  @Test
  public void should_detect_empty_strings() {
    assertTrue(Util.isEmpty(null));
    assertTrue(Util.isEmpty(""));
    assertFalse(Util.isEmpty("value"));
  }
}
