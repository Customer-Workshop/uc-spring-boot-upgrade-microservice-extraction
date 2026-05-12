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
  void should_create_article_success() {
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
  void should_create_article_with_empty_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("No Tags Article")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList())
            .build();

    Article result = articleCommandService.createArticle(param, user);

    assertNotNull(result);
    assertTrue(result.getTags().isEmpty());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void should_update_article_title() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Arrays.asList("java"), user.getId());

    UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "", "");
    Article result = articleCommandService.updateArticle(article, updateParam);

    assertEquals("New Title", result.getTitle());
    assertEquals("new-title", result.getSlug());
    verify(articleRepository).save(article);
  }

  @Test
  void should_update_article_body_and_description() {
    Article article =
        new Article("Title", "Old Desc", "Old Body", Arrays.asList("java"), user.getId());

    UpdateArticleParam updateParam = new UpdateArticleParam("", "New Body", "New Desc");
    Article result = articleCommandService.updateArticle(article, updateParam);

    assertEquals("New Body", result.getBody());
    assertEquals("New Desc", result.getDescription());
    verify(articleRepository).save(article);
  }

  @Test
  void should_not_change_when_all_params_empty() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());
    String originalSlug = article.getSlug();

    UpdateArticleParam updateParam = new UpdateArticleParam("", "", "");
    Article result = articleCommandService.updateArticle(article, updateParam);

    assertEquals("Title", result.getTitle());
    assertEquals(originalSlug, result.getSlug());
    verify(articleRepository).save(article);
  }
}
