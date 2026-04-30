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
import org.mockito.Mockito;

public class ArticleCommandServiceTest {

  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;
  private User user;

  @BeforeEach
  public void setUp() {
    articleRepository = Mockito.mock(ArticleRepository.class);
    articleCommandService = new ArticleCommandService(articleRepository);
    user = new User("test@test.com", "testuser", "123", "", "");
  }

  @Test
  public void should_create_article() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article result = articleCommandService.createArticle(param, user);
    assertNotNull(result);
    assertEquals("Test Title", result.getTitle());
    assertEquals("Test Description", result.getDescription());
    assertEquals("Test Body", result.getBody());
    assertEquals(user.getId(), result.getUserId());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_create_article_with_empty_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags")
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
    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "new body", "new desc");

    Article result = articleCommandService.updateArticle(article, updateParam);
    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    assertEquals("new body", result.getBody());
    assertEquals("new desc", result.getDescription());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article_with_partial_fields() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "", "");

    Article result = articleCommandService.updateArticle(article, updateParam);
    assertNotNull(result);
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_build_new_article_param_with_all_fields() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("tag1"))
            .build();
    assertEquals("title", param.getTitle());
    assertEquals("desc", param.getDescription());
    assertEquals("body", param.getBody());
    assertEquals(1, param.getTagList().size());
  }

  @Test
  public void should_create_new_article_param_with_no_args_constructor() {
    NewArticleParam param = new NewArticleParam();
    assertNull(param.getTitle());
    assertNull(param.getDescription());
    assertNull(param.getBody());
    assertNull(param.getTagList());
  }

  @Test
  public void should_create_new_article_param_with_all_args_constructor() {
    NewArticleParam param = new NewArticleParam("title", "desc", "body", Arrays.asList("tag"));
    assertEquals("title", param.getTitle());
    assertEquals("desc", param.getDescription());
    assertEquals("body", param.getBody());
    assertEquals(1, param.getTagList().size());
  }

  @Test
  public void should_create_update_article_param() {
    UpdateArticleParam param = new UpdateArticleParam("title", "body", "desc");
    assertEquals("title", param.getTitle());
    assertEquals("body", param.getBody());
    assertEquals("desc", param.getDescription());
  }
}
