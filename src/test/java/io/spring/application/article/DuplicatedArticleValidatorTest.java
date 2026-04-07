package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DuplicatedArticleValidatorTest {

  private DuplicatedArticleValidator validator;
  private ArticleQueryService articleQueryService;
  private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    validator = new DuplicatedArticleValidator();
    articleQueryService = mock(ArticleQueryService.class);
    ReflectionTestUtils.setField(validator, "articleQueryService", articleQueryService);
    context = mock(ConstraintValidatorContext.class);
  }

  @Test
  void should_return_true_when_article_does_not_exist() {
    when(articleQueryService.findBySlug(any(), eq(null))).thenReturn(Optional.empty());

    assertTrue(validator.isValid("New Article Title", context));
  }

  @Test
  void should_return_false_when_article_already_exists() {
    ArticleData existing = mock(ArticleData.class);
    when(articleQueryService.findBySlug(any(), eq(null))).thenReturn(Optional.of(existing));

    assertFalse(validator.isValid("Existing Title", context));
  }
}
