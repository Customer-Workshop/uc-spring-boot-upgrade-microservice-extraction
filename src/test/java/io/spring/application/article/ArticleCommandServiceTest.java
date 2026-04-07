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

class ArticleCommandServiceTest {

  private ArticleCommandService articleCommandService;
  private ArticleRepository articleRepository;
  private User user;

  @BeforeEach
  void setUp() {
    articleRepository = mock(ArticleRepository.class);
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("test@example.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_create_article_with_all_fields() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("Test Title", result.getTitle());
    assertEquals("Test Description", result.getDescription());
    assertEquals("Test Body", result.getBody());
    assertEquals(user.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_with_empty_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Collections.emptyList())
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertTrue(result.getTags().isEmpty());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article_title() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Collections.emptyList(), user.getId());

    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    assertEquals("New Body", result.getBody());
    assertEquals("New Desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_with_empty_strings() {
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());

    UpdateArticleParam param = new UpdateArticleParam("", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    verify(articleRepository).save(article);
  }
}
