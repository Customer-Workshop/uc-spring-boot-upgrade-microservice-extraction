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
    user = new User("test@test.com", "testuser", "password", "", "");
  }

  @Test
  void should_create_article_success() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("desc")
            .body("body content")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("test-article", result.getSlug());
    assertEquals("Test Article", result.getTitle());
    assertEquals("desc", result.getDescription());
    assertEquals("body content", result.getBody());
    assertEquals(user.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_with_empty_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("desc")
            .body("body")
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
        new Article("Old Title", "old desc", "old body", Arrays.asList("java"), user.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "", "");

    Article result = articleCommandService.updateArticle(article, updateParam);

    assertNotNull(result);
    assertEquals("new-title", result.getSlug());
    assertEquals("New Title", result.getTitle());
    verify(articleRepository).save(eq(article));
  }

  @Test
  void should_update_article_body_and_description() {
    Article article =
        new Article("Title", "old desc", "old body", Arrays.asList("java"), user.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("", "new body", "new desc");

    Article result = articleCommandService.updateArticle(article, updateParam);

    assertNotNull(result);
    assertEquals("new body", result.getBody());
    assertEquals("new desc", result.getDescription());
    verify(articleRepository).save(eq(article));
  }

  @Test
  void should_update_article_all_fields() {
    Article article =
        new Article("Old Title", "old desc", "old body", Arrays.asList("java"), user.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "new body", "new desc");

    Article result = articleCommandService.updateArticle(article, updateParam);

    assertEquals("New Title", result.getTitle());
    assertEquals("new body", result.getBody());
    assertEquals("new desc", result.getDescription());
    verify(articleRepository).save(eq(article));
  }

  @Test
  void should_not_modify_article_when_all_params_empty() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    String originalSlug = article.getSlug();
    UpdateArticleParam updateParam = new UpdateArticleParam("", "", "");

    Article result = articleCommandService.updateArticle(article, updateParam);

    assertEquals(originalSlug, result.getSlug());
    assertEquals("Title", result.getTitle());
    verify(articleRepository).save(eq(article));
  }
}
