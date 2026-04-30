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
    user = new User("test@example.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_create_article_with_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("Description")
            .body("Body content")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, user);

    assertNotNull(article);
    assertEquals("test-article", article.getSlug());
    assertEquals("Test Article", article.getTitle());
    assertEquals("Description", article.getDescription());
    assertEquals("Body content", article.getBody());
    assertEquals(user.getId(), article.getUserId());
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

    Article article = articleCommandService.createArticle(param, user);

    assertNotNull(article);
    assertTrue(article.getTags().isEmpty());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article_title() {
    Article article =
        new Article("Original Title", "desc", "body", Arrays.asList("java"), user.getId());

    UpdateArticleParam param = new UpdateArticleParam("Updated Title", null, null);

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("Updated Title", updated.getTitle());
    assertEquals("updated-title", updated.getSlug());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_body() {
    Article article =
        new Article("Title", "desc", "original body", Arrays.asList("java"), user.getId());

    UpdateArticleParam param = new UpdateArticleParam(null, "updated body", null);

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("updated body", updated.getBody());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_description() {
    Article article =
        new Article("Title", "original desc", "body", Arrays.asList("java"), user.getId());

    UpdateArticleParam param = new UpdateArticleParam(null, null, "updated desc");

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("updated desc", updated.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_all_fields() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());

    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Desc");

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", updated.getTitle());
    assertEquals("New Body", updated.getBody());
    assertEquals("New Desc", updated.getDescription());
    verify(articleRepository).save(article);
  }
}
