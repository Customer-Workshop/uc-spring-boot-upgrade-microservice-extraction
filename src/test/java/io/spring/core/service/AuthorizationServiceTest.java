package io.spring.core.service;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.article.Article;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuthorizationServiceTest {

  private User articleAuthor;
  private User otherUser;
  private Article article;

  @BeforeEach
  public void setUp() {
    articleAuthor = new User("author@test.com", "author", "123", "", "");
    otherUser = new User("other@test.com", "other", "123", "", "");
    article = new Article("Title", "desc", "body", Arrays.asList("java"), articleAuthor.getId());
  }

  @Test
  public void should_instantiate() {
    AuthorizationService service = new AuthorizationService();
    assertNotNull(service);
  }

  @Test
  public void should_allow_author_to_write_article() {
    assertTrue(AuthorizationService.canWriteArticle(articleAuthor, article));
  }

  @Test
  public void should_deny_non_author_to_write_article() {
    assertFalse(AuthorizationService.canWriteArticle(otherUser, article));
  }

  @Test
  public void should_allow_article_author_to_write_comment() {
    Comment comment = new Comment("body", otherUser.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(articleAuthor, article, comment));
  }

  @Test
  public void should_allow_comment_author_to_write_comment() {
    Comment comment = new Comment("body", otherUser.getId(), article.getId());
    assertTrue(AuthorizationService.canWriteComment(otherUser, article, comment));
  }

  @Test
  public void should_deny_unrelated_user_to_write_comment() {
    User thirdUser = new User("third@test.com", "third", "123", "", "");
    Comment comment = new Comment("body", otherUser.getId(), article.getId());
    assertFalse(AuthorizationService.canWriteComment(thirdUser, article, comment));
  }
}
