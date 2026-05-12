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
    user = new User("test@test.com", "testuser", "123", "", "");
  }

  @Test
  public void should_create_article_success() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("Description")
            .body("Body content")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, user);

    assertNotNull(article);
    assertEquals("Test Article", article.getTitle());
    assertEquals("Description", article.getDescription());
    assertEquals("Body content", article.getBody());
    assertEquals(user.getId(), article.getUserId());
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

    Article article = articleCommandService.createArticle(param, user);
    assertNotNull(article);
    assertTrue(article.getTags().isEmpty());
  }

  @Test
  public void should_update_article_success() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Arrays.asList("java"), user.getId());

    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "New Body", "New Desc");
    Article updated = articleCommandService.updateArticle(article, updateParam);

    assertNotNull(updated);
    assertEquals("New Title", updated.getTitle());
    assertEquals("New Body", updated.getBody());
    assertEquals("New Desc", updated.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  public void should_update_article_with_partial_params() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Arrays.asList("java"), user.getId());

    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "", "");
    Article updated = articleCommandService.updateArticle(article, updateParam);

    assertNotNull(updated);
    assertEquals("New Title", updated.getTitle());
    verify(articleRepository).save(article);
  }
}
