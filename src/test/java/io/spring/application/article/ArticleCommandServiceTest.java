package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ArticleCommandServiceTest {

  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;

  @BeforeEach
  public void setUp() {
    articleRepository = mock(ArticleRepository.class);
    articleCommandService = new ArticleCommandService(articleRepository);
  }

  @Test
  public void should_create_article() {
    User creator = new User("email@test.com", "username", "password", "", "");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Desc")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, creator);

    assertNotNull(article);
    assertEquals("Test Title", article.getTitle());
    assertEquals("Test Desc", article.getDescription());
    assertEquals("Test Body", article.getBody());
    assertEquals("test-title", article.getSlug());
    assertEquals(creator.getId(), article.getUserId());

    ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
    verify(articleRepository).save(captor.capture());
    assertEquals("Test Title", captor.getValue().getTitle());
  }

  @Test
  public void should_update_article() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Arrays.asList("java"), "userId");
    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Desc");

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", updated.getTitle());
    assertEquals("new-title", updated.getSlug());
    verify(articleRepository).save(article);
  }

  @Test
  public void should_save_article_on_create() {
    User creator = new User("email@test.com", "username", "password", "", "");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Title")
            .description("Desc")
            .body("Body")
            .tagList(Arrays.asList())
            .build();

    articleCommandService.createArticle(param, creator);

    verify(articleRepository, times(1)).save(any(Article.class));
  }

  @Test
  public void should_save_article_on_update() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), "userId");
    UpdateArticleParam param = new UpdateArticleParam("Updated Title", "", "");

    articleCommandService.updateArticle(article, param);

    verify(articleRepository, times(1)).save(article);
  }
}
