package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  private ArticleCommandService service;
  private User user;

  @BeforeEach
  void setUp() {
    service = new ArticleCommandService(articleRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_create_article() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("desc")
            .body("body content")
            .tagList(List.of("java", "spring"))
            .build();

    Article article = service.createArticle(param, user);

    assertNotNull(article);
    assertEquals("test-article", article.getSlug());
    assertEquals("Test Article", article.getTitle());
    assertEquals("desc", article.getDescription());
    assertEquals("body content", article.getBody());
    assertEquals(user.getId(), article.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article() {
    Article article =
        new Article("Original Title", "orig desc", "orig body", List.of(), user.getId());

    UpdateArticleParam updateParam =
        new UpdateArticleParam("Updated Title", "updated body", "updated desc");

    Article updatedArticle = service.updateArticle(article, updateParam);

    assertNotNull(updatedArticle);
    assertEquals("Updated Title", updatedArticle.getTitle());
    assertEquals("updated body", updatedArticle.getBody());
    assertEquals("updated desc", updatedArticle.getDescription());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article_with_empty_params() {
    Article article =
        new Article("Original Title", "orig desc", "orig body", List.of(), user.getId());

    UpdateArticleParam updateParam = new UpdateArticleParam("", "", "");

    Article updatedArticle = service.updateArticle(article, updateParam);

    assertNotNull(updatedArticle);
    assertEquals("Original Title", updatedArticle.getTitle());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_new_article_param_with_builder() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("title")
            .description("desc")
            .body("body")
            .tagList(List.of("tag1"))
            .build();

    assertEquals("title", param.getTitle());
    assertEquals("desc", param.getDescription());
    assertEquals("body", param.getBody());
    assertEquals(1, param.getTagList().size());
  }
}
