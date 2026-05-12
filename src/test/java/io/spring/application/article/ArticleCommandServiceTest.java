package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleCommandService articleCommandService;

  @Test
  public void should_create_article_success() {
    User creator = new User("test@test.com", "testuser", "password", "bio", "image");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, creator);

    assertNotNull(article);
    assertEquals("test-article", article.getSlug());
    assertEquals("Test Article", article.getTitle());
    assertEquals("Test Description", article.getDescription());
    assertEquals("Test Body", article.getBody());
    assertEquals(creator.getId(), article.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article_success() {
    User creator = new User("test@test.com", "testuser", "password", "bio", "image");
    Article article =
        new Article(
            "Original Title",
            "Original Desc",
            "Original Body",
            Arrays.asList("java"),
            creator.getId());
    UpdateArticleParam updateParam =
        new UpdateArticleParam("New Title", "New Body", "New Description");

    Article updated = articleCommandService.updateArticle(article, updateParam);

    assertNotNull(updated);
    assertEquals("New Title", updated.getTitle());
    assertEquals("New Body", updated.getBody());
    assertEquals("New Description", updated.getDescription());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article_with_empty_fields() {
    User creator = new User("test@test.com", "testuser", "password", "bio", "image");
    Article article =
        new Article(
            "Original Title",
            "Original Desc",
            "Original Body",
            Arrays.asList("java"),
            creator.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("", "", "");

    Article updated = articleCommandService.updateArticle(article, updateParam);

    assertNotNull(updated);
    assertEquals("Original Title", updated.getTitle());
    assertEquals("Original Body", updated.getBody());
    assertEquals("Original Desc", updated.getDescription());
  }

  @Test
  public void should_create_article_with_no_tags() {
    User creator = new User("test@test.com", "testuser", "password", "bio", "image");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags Article")
            .description("Desc")
            .body("Body")
            .tagList(Arrays.asList())
            .build();

    Article article = articleCommandService.createArticle(param, creator);

    assertNotNull(article);
    assertTrue(article.getTags().isEmpty());
  }
}
