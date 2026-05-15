package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleCommandService articleCommandService;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image.png");
  }

  @Test
  void should_create_article_and_save() {
    NewArticleParam param =
        new NewArticleParam("Test Title", "desc", "body", Arrays.asList("java"));

    Article article = articleCommandService.createArticle(param, user);

    assertNotNull(article);
    assertEquals("test-title", article.getSlug());
    assertEquals("Test Title", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body", article.getBody());
    assertEquals(user.getId(), article.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article_title() {
    Article article =
        new Article("Original Title", "desc", "body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "", "");

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", updated.getTitle());
    assertEquals("new-title", updated.getSlug());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_description() {
    Article article = new Article("Title", "old desc", "body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "", "new desc");

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("new desc", updated.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_body() {
    Article article = new Article("Title", "desc", "old body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "new body", "");

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("new body", updated.getBody());
    verify(articleRepository).save(article);
  }
}
