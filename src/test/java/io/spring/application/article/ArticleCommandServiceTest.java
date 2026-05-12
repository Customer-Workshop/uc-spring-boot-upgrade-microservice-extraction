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
  public void should_create_article() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("My Article")
            .description("Description")
            .body("Article body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);
    assertNotNull(result);
    assertEquals("My Article", result.getTitle());
    assertEquals("Description", result.getDescription());
    assertEquals("Article body", result.getBody());
    assertEquals(user.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_create_article_without_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags Article")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList())
            .build();

    Article result = articleCommandService.createArticle(param, user);
    assertNotNull(result);
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article() {
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
  public void should_update_article_with_empty_fields() {
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());

    UpdateArticleParam param = new UpdateArticleParam("", "", "");

    Article result = articleCommandService.updateArticle(article, param);
    assertNotNull(result);
    verify(articleRepository).save(article);
  }
}
