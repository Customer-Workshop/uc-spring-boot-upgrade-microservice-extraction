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
  public void setUp() {
    articleRepository = mock(ArticleRepository.class);
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("test@test.com", "testuser", "pass", "", "");
  }

  @Test
  public void should_create_article_success() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Description")
            .body("Body content")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);
    assertNotNull(result);
    assertEquals("Test Title", result.getTitle());
    assertEquals("Description", result.getDescription());
    assertEquals("Body content", result.getBody());
    assertEquals(user.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_create_article_with_empty_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags Article")
            .description("Description")
            .body("Body")
            .tagList(Collections.emptyList())
            .build();

    Article result = articleCommandService.createArticle(param, user);
    assertNotNull(result);
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article_success() {
    Article article =
        new Article("Original Title", "Desc", "Body", Collections.emptyList(), user.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "New Body", "New Desc");

    Article result = articleCommandService.updateArticle(article, updateParam);
    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    assertEquals("New Body", result.getBody());
    assertEquals("New Desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  public void should_update_article_with_partial_fields() {
    Article article =
        new Article("Original Title", "Desc", "Body", Collections.emptyList(), user.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "", "");

    Article result = articleCommandService.updateArticle(article, updateParam);
    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    verify(articleRepository).save(article);
  }
}
