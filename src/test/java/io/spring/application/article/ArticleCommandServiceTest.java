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
    user = new User("test@test.com", "testuser", "password", "", "");
  }

  @Test
  void should_create_article_success() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("A test article")
            .body("Article body content")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("test-article", result.getSlug());
    assertEquals("Test Article", result.getTitle());
    assertEquals("A test article", result.getDescription());
    assertEquals("Article body content", result.getBody());
    assertEquals(user.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_with_empty_tag_list() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList())
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("no-tags", result.getSlug());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_create_article_with_single_tag() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Single Tag")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("kotlin"))
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertEquals("single-tag", result.getSlug());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article_title() {
    Article article =
        new Article("Original Title", "desc", "body", Arrays.asList("java"), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("New Title", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    assertEquals("new-title", result.getSlug());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_body_and_description() {
    Article article = new Article("Title", "old desc", "old body", Arrays.asList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "new body", "new desc");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("new body", result.getBody());
    assertEquals("new desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_not_change_article_when_empty_params() {
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    UpdateArticleParam param = new UpdateArticleParam("", "", "");

    Article result = articleCommandService.updateArticle(article, param);

    assertNotNull(result);
    assertEquals("Title", result.getTitle());
    assertEquals("desc", result.getDescription());
    assertEquals("body", result.getBody());
    verify(articleRepository).save(article);
  }
}
