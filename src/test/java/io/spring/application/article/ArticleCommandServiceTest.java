package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleCommandServiceTest {

  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleCommandService articleCommandService;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "bio", "image");
  }

  @Test
  public void should_create_article_and_save_it() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("a new title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, user);

    assertEquals("a-new-title", article.getSlug());
    assertEquals(user.getId(), article.getUserId());
    assertEquals(
        new HashSet<>(Arrays.asList("java", "spring")),
        article.getTags().stream().map(Tag::getName).collect(Collectors.toSet()));
    ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
    verify(articleRepository).save(captor.capture());
    assertEquals(article.getId(), captor.getValue().getId());
  }

  @Test
  public void should_create_article_without_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("title")
            .description("desc")
            .body("body")
            .tagList(Collections.emptyList())
            .build();

    assertTrue(articleCommandService.createArticle(param, user).getTags().isEmpty());
  }

  @Test
  public void should_update_article_and_save_it() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());

    Article updated =
        articleCommandService.updateArticle(
            article, new UpdateArticleParam("new title", "new body", "new desc"));

    assertEquals("new title", updated.getTitle());
    assertEquals("new body", updated.getBody());
    assertEquals("new desc", updated.getDescription());
    assertEquals("new-title", updated.getSlug());
    verify(articleRepository).save(article);
  }

  @Test
  public void should_keep_current_values_when_update_param_is_empty() {
    Article article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());

    Article updated =
        articleCommandService.updateArticle(article, new UpdateArticleParam("", "", ""));

    assertEquals("title", updated.getTitle());
    assertEquals("body", updated.getBody());
    assertEquals("desc", updated.getDescription());
  }

  @Test
  public void should_report_violations_for_blank_new_article_param() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    NewArticleParam param =
        NewArticleParam.builder().title("").description("").body("").tagList(null).build();

    Set<ConstraintViolation<NewArticleParam>> violations =
        validator.validateProperty(param, "description");
    List<String> messages =
        violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());

    assertEquals(Collections.singletonList("can't be empty"), messages);
  }

  @Test
  public void should_not_save_article_when_creator_is_missing() {
    NewArticleParam param =
        NewArticleParam.builder().title("title").description("desc").body("body").build();

    assertThrows(
        NullPointerException.class, () -> articleCommandService.createArticle(param, null));
    verify(articleRepository, never()).save(any());
  }
}
