package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
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
  public void setUp() {
    articleCommandService = new ArticleCommandService(articleRepository);
  }

  @Test
  public void should_create_article_and_save() {
    User creator = new User("test@example.com", "testuser", "password", "", "");
    NewArticleParam param =
        new NewArticleParam("Test Title", "Test Description", "Test Body", Arrays.asList("java"));

    Article article = articleCommandService.createArticle(param, creator);

    assertNotNull(article);
    assertEquals("Test Title", article.getTitle());
    assertEquals("Test Description", article.getDescription());
    assertEquals("Test Body", article.getBody());
    assertEquals("test-title", article.getSlug());
    assertEquals(creator.getId(), article.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_create_article_with_tags() {
    User creator = new User("test@example.com", "testuser", "password", "", "");
    NewArticleParam param =
        new NewArticleParam(
            "Tagged Article", "desc", "body", Arrays.asList("java", "spring", "test"));

    Article article = articleCommandService.createArticle(param, creator);

    assertNotNull(article);
    assertEquals(3, article.getTags().size());
    verify(articleRepository).save(article);
  }

  @Test
  public void should_update_article_title() {
    Article article = new Article("Old Title", "desc", "body", Arrays.asList("java"), "user1");
    UpdateArticleParam param = new UpdateArticleParam("New Title", "", "");
    // UpdateArticleParam field order: title, body, description

    Article updated = articleCommandService.updateArticle(article, param);

    assertNotNull(updated);
    assertEquals("New Title", updated.getTitle());
    assertEquals("new-title", updated.getSlug());
    verify(articleRepository).save(article);
  }

  @Test
  public void should_update_article_body_and_description() {
    Article article = new Article("Title", "old desc", "old body", Arrays.asList("java"), "user1");
    UpdateArticleParam param = new UpdateArticleParam("", "new body", "new desc");

    Article updated = articleCommandService.updateArticle(article, param);

    assertNotNull(updated);
    assertEquals("new desc", updated.getDescription());
    assertEquals("new body", updated.getBody());
    verify(articleRepository).save(article);
  }
}
