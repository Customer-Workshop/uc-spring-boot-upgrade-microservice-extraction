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

public class ArticleCommandServiceTest {

  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;
  private User user;

  @BeforeEach
  void setUp() {
    articleRepository = mock(ArticleRepository.class);
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("test@test.com", "testuser", "pass", "", "");
  }

  @Test
  void should_create_article_success() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("description")
            .body("body content")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("test-title", result.getSlug());
    assertEquals("description", result.getDescription());
    assertEquals("body content", result.getBody());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_with_empty_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags Article")
            .description("desc")
            .body("body")
            .tagList(Collections.emptyList())
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertTrue(result.getTags().isEmpty());
  }

  @Test
  void should_update_article_success() {
    Article article =
        new Article("Old Title", "old desc", "old body", Arrays.asList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "new body", "new desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("new-title", result.getSlug());
    assertEquals("new body", result.getBody());
    assertEquals("new desc", result.getDescription());
    verify(articleRepository).save(eq(article));
  }

  @Test
  void should_update_article_with_empty_params() {
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    verify(articleRepository).save(eq(article));
  }
}
