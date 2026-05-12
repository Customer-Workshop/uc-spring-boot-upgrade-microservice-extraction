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
  void should_create_article_success() {
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
  void should_create_article_with_empty_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags")
            .description("desc")
            .body("body")
            .tagList(Collections.emptyList())
            .build();

    Article article = articleCommandService.createArticle(param, user);

    assertNotNull(article);
    assertTrue(article.getTags().isEmpty());
  }

  @Test
  void should_update_article_title() {
    Article article = new Article("Old Title", "desc", "body", Arrays.asList("java"), user.getId());

    UpdateArticleParam param = new UpdateArticleParam("New Title", "", "");
    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("New Title", updated.getTitle());
    assertEquals("new-title", updated.getSlug());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_body() {
    Article article = new Article("Title", "desc", "old body", Arrays.asList("java"), user.getId());

    UpdateArticleParam param = new UpdateArticleParam("", "new body", "");
    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("new body", updated.getBody());
  }

  @Test
  void should_update_article_description() {
    Article article = new Article("Title", "old desc", "body", Arrays.asList("java"), user.getId());

    UpdateArticleParam param = new UpdateArticleParam("", "", "new desc");
    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals("new desc", updated.getDescription());
  }

  @Test
  void should_not_change_article_when_empty_params() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    String originalSlug = article.getSlug();

    UpdateArticleParam param = new UpdateArticleParam("", "", "");
    Article updated = articleCommandService.updateArticle(article, param);

    assertEquals(originalSlug, updated.getSlug());
    assertEquals("desc", updated.getDescription());
    assertEquals("body", updated.getBody());
  }
}
