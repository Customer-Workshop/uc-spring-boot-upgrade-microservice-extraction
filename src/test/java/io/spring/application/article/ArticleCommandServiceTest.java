package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
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
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_create_article() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("test-article", result.getSlug());
    assertEquals("Test Article", result.getTitle());
    assertEquals("desc", result.getDescription());
    assertEquals("body", result.getBody());
    assertEquals(user.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article() {
    Article article =
        new Article("Old Title", "old desc", "old body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "new body", "new desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    assertEquals("new body", result.getBody());
    assertEquals("new desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_with_empty_params() {
    Article article =
        new Article("Old Title", "old desc", "old body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("Old Title", result.getTitle());
    assertEquals("old body", result.getBody());
    assertEquals("old desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_title_only() {
    Article article =
        new Article("Old Title", "old desc", "old body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", result.getTitle());
    assertEquals("old body", result.getBody());
    assertEquals("old desc", result.getDescription());
  }
}
