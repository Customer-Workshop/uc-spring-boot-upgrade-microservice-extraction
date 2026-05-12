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
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  public void should_create_article_successfully() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, user);

    assertNotNull(article);
    assertEquals("Test Title", article.getTitle());
    assertEquals("Test Description", article.getDescription());
    assertEquals("Test Body", article.getBody());
    assertEquals(user.getId(), article.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_create_article_with_empty_tag_list() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags")
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
  public void should_update_article_title() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Collections.emptyList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "", "");

    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", updated.getTitle());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article_body_and_description() {
    Article article =
        new Article("Title", "Old Desc", "Old Body", Collections.emptyList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "New Body", "New Description");

    Article updated = articleCommandService.updateArticle(article, param);

    assertNotNull(updated);
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article_with_all_fields() {
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    UpdateArticleParam param =
        new UpdateArticleParam("Updated Title", "Updated Body", "Updated Desc");

    Article updated = articleCommandService.updateArticle(article, param);

    assertNotNull(updated);
    verify(articleRepository).save(any(Article.class));
  }
}
