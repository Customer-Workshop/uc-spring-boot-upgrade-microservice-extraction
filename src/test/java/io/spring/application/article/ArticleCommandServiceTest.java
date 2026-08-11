package io.spring.application.article;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.Assertions;
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
    user = new User("a@test.com", "a", "123", "", "");
  }

  @Test
  public void should_create_article_success() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("a new title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, user);

    Assertions.assertEquals("a new title", article.getTitle());
    Assertions.assertEquals("a-new-title", article.getSlug());
    Assertions.assertEquals(user.getId(), article.getUserId());
    Assertions.assertEquals(2, article.getTags().size());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  public void should_update_article_success() {
    Article article =
        new Article("title", "desc", "body", Collections.singletonList("java"), user.getId());

    Article updated =
        articleCommandService.updateArticle(
            article, new UpdateArticleParam("new title", "new body", "new desc"));

    Assertions.assertEquals("new title", updated.getTitle());
    Assertions.assertEquals("new body", updated.getBody());
    Assertions.assertEquals("new desc", updated.getDescription());
    Assertions.assertEquals("new-title", updated.getSlug());
    verify(articleRepository).save(article);
  }

  @Test
  public void should_reject_blank_new_article_fields() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    NewArticleParam param = NewArticleParam.builder().title("").description("").body("").build();

    Set<ConstraintViolation<NewArticleParam>> violations =
        validator.validateProperty(param, "description");

    Assertions.assertEquals(1, violations.size());
    Assertions.assertEquals("can't be empty", violations.iterator().next().getMessage());
    Assertions.assertTrue(validator.validateProperty(param, "body").size() == 1);
  }

  @Test
  public void should_keep_empty_defaults_on_update_param() {
    UpdateArticleParam param = new UpdateArticleParam();

    Assertions.assertEquals("", param.getTitle());
    Assertions.assertEquals("", param.getBody());
    Assertions.assertEquals("", param.getDescription());
  }
}
