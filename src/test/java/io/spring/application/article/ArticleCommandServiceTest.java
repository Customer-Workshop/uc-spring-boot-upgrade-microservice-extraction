package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
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
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService articleCommandService;

  @BeforeEach
  void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
  }

  @Test
  void should_create_article_successfully() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("A test description")
            .body("Article body content")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("Test Article", result.getTitle());
    assertEquals("A test description", result.getDescription());
    assertEquals("Article body content", result.getBody());
    assertEquals(user.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_with_empty_tags() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags Article")
            .description("Desc")
            .body("Body")
            .tagList(Collections.emptyList())
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertTrue(result.getTags().isEmpty());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article_title() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Collections.emptyList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", result.getTitle());
    assertEquals("new-title", result.getSlug());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_description() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    Article article =
        new Article("Title", "Old Desc", "Body", Collections.emptyList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "", "New Desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("New Desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_body() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    Article article =
        new Article("Title", "Desc", "Old Body", Collections.emptyList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "New Body", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("New Body", result.getBody());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_all_fields() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", result.getTitle());
    assertEquals("New Body", result.getBody());
    assertEquals("New Desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_not_change_fields_when_empty_update() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertEquals("Title", result.getTitle());
    assertEquals("Body", result.getBody());
    assertEquals("Desc", result.getDescription());
    verify(articleRepository).save(article);
  }
}
