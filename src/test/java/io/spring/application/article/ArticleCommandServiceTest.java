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

  @BeforeEach
  void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
  }

  @Test
  void should_create_article() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, user);

    assertNotNull(article);
    assertEquals("test-title", article.getSlug());
    assertEquals("Test Title", article.getTitle());
    assertEquals("Test Description", article.getDescription());
    assertEquals("Test Body", article.getBody());
    assertEquals(user.getId(), article.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_with_empty_tags() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Title")
            .description("Desc")
            .body("Body")
            .tagList(Collections.emptyList())
            .build();

    Article article = articleCommandService.createArticle(param, user);

    assertNotNull(article);
    assertTrue(article.getTags().isEmpty());
  }

  @Test
  void should_update_article() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Collections.emptyList(), user.getId());

    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Desc");

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", updated.getTitle());
    assertEquals("New Body", updated.getBody());
    assertEquals("New Desc", updated.getDescription());
    verify(articleRepository).save(eq(article));
  }

  @Test
  void should_update_article_with_empty_fields() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    String originalTitle = article.getTitle();

    UpdateArticleParam param = new UpdateArticleParam("", "", "");

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals(originalTitle, updated.getTitle());
    verify(articleRepository).save(eq(article));
  }
}
