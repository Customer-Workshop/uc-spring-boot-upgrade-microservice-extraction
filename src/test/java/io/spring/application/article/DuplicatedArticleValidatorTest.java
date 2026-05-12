package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DuplicatedArticleValidatorTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private ConstraintValidatorContext context;

  @InjectMocks private DuplicatedArticleValidator validator;

  @Test
  void should_return_true_when_article_title_is_unique() {
    when(articleQueryService.findBySlug(eq("test-title"), any())).thenReturn(Optional.empty());

    assertTrue(validator.isValid("test title", context));
  }

  @Test
  void should_return_false_when_article_title_already_exists() {
    ArticleData existingArticle = mock(ArticleData.class);
    when(articleQueryService.findBySlug(eq("existing-title"), any()))
        .thenReturn(Optional.of(existingArticle));

    assertFalse(validator.isValid("existing title", context));
  }
}
