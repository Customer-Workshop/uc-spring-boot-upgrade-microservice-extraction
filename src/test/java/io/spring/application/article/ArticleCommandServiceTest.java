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
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
  }

  @Test
  void should_create_article_with_tags() {
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
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_without_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags Article")
            .description("Desc")
            .body("Body")
            .tagList(Collections.emptyList())
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("no-tags-article", result.getSlug());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Arrays.asList("java"), user.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "New Body", "New Desc");

    Article result = articleCommandService.updateArticle(article, updateParam);

    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    assertEquals("New Body", result.getBody());
    assertEquals("New Desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_with_partial_params() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Arrays.asList(), user.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", null, null);

    Article result = articleCommandService.updateArticle(article, updateParam);

    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_with_all_null_params() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam(null, null, null);

    Article result = articleCommandService.updateArticle(article, updateParam);

    assertNotNull(result);
    assertEquals("Title", result.getTitle());
    verify(articleRepository).save(article);
  }
}
