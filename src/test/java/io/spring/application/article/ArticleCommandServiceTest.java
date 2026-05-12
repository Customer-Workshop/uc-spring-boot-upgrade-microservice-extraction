package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;
  private User user;

  @BeforeEach
  void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
  }

  @Test
  void should_create_article_successfully() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("A test article")
            .body("article body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, user);
    assertNotNull(article);
    assertEquals("test-article", article.getSlug());
    assertEquals("Test Article", article.getTitle());
    assertEquals("A test article", article.getDescription());
    assertEquals("article body", article.getBody());
    assertEquals(user.getId(), article.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_with_empty_tag_list() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("A test article")
            .body("article body")
            .tagList(Collections.emptyList())
            .build();

    Article article = articleCommandService.createArticle(param, user);
    assertNotNull(article);
    assertTrue(article.getTags().isEmpty());
  }

  @Test
  void should_update_article_successfully() {
    Article article = new Article("Original", "desc", "body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "new body", "new desc");

    Article updated = articleCommandService.updateArticle(article, param);
    assertNotNull(updated);
    assertEquals("New Title", updated.getTitle());
    assertEquals("new body", updated.getBody());
    assertEquals("new desc", updated.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_with_partial_params() {
    Article article = new Article("Original", "desc", "body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "", "");

    Article updated = articleCommandService.updateArticle(article, param);
    assertNotNull(updated);
    assertEquals("New Title", updated.getTitle());
    assertEquals("body", updated.getBody());
    assertEquals("desc", updated.getDescription());
  }

  @Test
  void should_not_change_article_with_empty_params() {
    Article article = new Article("Original", "desc", "body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "", "");

    Article updated = articleCommandService.updateArticle(article, param);
    assertEquals("Original", updated.getTitle());
    assertEquals("body", updated.getBody());
    assertEquals("desc", updated.getDescription());
  }
}
