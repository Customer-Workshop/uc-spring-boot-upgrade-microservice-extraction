package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class DuplicatedArticleValidatorTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private ConstraintValidatorContext context;

  private DuplicatedArticleValidator validator;

  @BeforeEach
  void setUp() {
    validator = new DuplicatedArticleValidator();
    ReflectionTestUtils.setField(validator, "articleQueryService", articleQueryService);
  }

  @Test
  void should_return_true_when_title_is_unique() {
    when(articleQueryService.findBySlug(eq("unique-title"), eq(null))).thenReturn(Optional.empty());

    boolean result = validator.isValid("Unique Title", context);

    assertTrue(result);
  }

  @Test
  void should_return_false_when_title_is_duplicated() {
    ArticleData existingArticle = mock(ArticleData.class);
    when(articleQueryService.findBySlug(eq("existing-title"), eq(null)))
        .thenReturn(Optional.of(existingArticle));

    boolean result = validator.isValid("Existing Title", context);

    assertFalse(result);
  }
}
