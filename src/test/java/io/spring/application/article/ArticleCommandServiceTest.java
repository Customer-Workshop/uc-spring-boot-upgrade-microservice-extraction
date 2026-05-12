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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;
  private User user;

  @BeforeEach
  void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_create_article_success() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("test-title", result.getSlug());
    assertEquals("Test Title", result.getTitle());
    assertEquals("Test Description", result.getDescription());
    assertEquals("Test Body", result.getBody());
    assertEquals(user.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_with_empty_tag_list() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList())
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertTrue(result.getTags().isEmpty());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article_success() {
    Article article =
        new Article(
            "Old Title", "Old Description", "Old Body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Description");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    assertEquals("New Body", result.getBody());
    assertEquals("New Description", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_with_empty_fields() {
    Article article =
        new Article("Title", "Description", "Body", Arrays.asList("java"), user.getId());
    String originalTitle = article.getTitle();
    UpdateArticleParam param = new UpdateArticleParam("", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals(originalTitle, result.getTitle());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_partial_fields() {
    Article article =
        new Article(
            "Old Title", "Old Description", "Old Body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", result.getTitle());
    assertEquals("Old Body", result.getBody());
    assertEquals("Old Description", result.getDescription());
    verify(articleRepository).save(article);
  }
}
