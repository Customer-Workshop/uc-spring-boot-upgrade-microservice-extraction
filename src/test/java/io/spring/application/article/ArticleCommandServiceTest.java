package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

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
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;
  private User user;

  @BeforeEach
  public void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
  }

  @Test
  public void should_create_article_successfully() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("Test Title", result.getTitle());
    assertEquals("desc", result.getDescription());
    assertEquals("body", result.getBody());
    assertEquals(user.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_create_article_with_empty_tags() {
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
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article_title() {
    Article article =
        new Article("Old Title", "old desc", "old body", Collections.emptyList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", result.getTitle());
    assertEquals("new-title", result.getSlug());
    verify(articleRepository).save(article);
  }

  @Test
  public void should_update_article_body_and_description() {
    Article article =
        new Article("Title", "old desc", "old body", Collections.emptyList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "new body", "new desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("new body", result.getBody());
    assertEquals("new desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  public void should_update_article_all_fields() {
    Article article =
        new Article("Title", "old desc", "old body", Collections.emptyList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "new body", "new desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", result.getTitle());
    assertEquals("new body", result.getBody());
    assertEquals("new desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  public void should_not_update_article_when_all_empty() {
    Article article = new Article("Title", "desc", "body", Collections.emptyList(), user.getId());
    String originalSlug = article.getSlug();
    UpdateArticleParam param = new UpdateArticleParam("", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("Title", result.getTitle());
    assertEquals(originalSlug, result.getSlug());
    verify(articleRepository).save(article);
  }
}
