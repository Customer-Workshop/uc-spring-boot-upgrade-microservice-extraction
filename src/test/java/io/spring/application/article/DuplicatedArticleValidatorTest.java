package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class DuplicatedArticleValidatorTest {

  @Mock private ArticleQueryService articleQueryService;

  private DuplicatedArticleValidator validator;

  @BeforeEach
  void setUp() {
    validator = new DuplicatedArticleValidator();
    ReflectionTestUtils.setField(validator, "articleQueryService", articleQueryService);
  }

  @Test
  void should_return_true_when_article_does_not_exist() {
    when(articleQueryService.findBySlug(any(), eq(null))).thenReturn(Optional.empty());

    boolean result = validator.isValid("Unique Title", null);

    assertTrue(result);
  }

  @Test
  void should_return_false_when_article_exists() {
    DateTime now = new DateTime();
    ArticleData existing =
        new ArticleData(
            "id",
            "duplicate-title",
            "Duplicate Title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData("uid", "user", "", "", false));
    when(articleQueryService.findBySlug(eq("duplicate-title"), eq(null)))
        .thenReturn(Optional.of(existing));

    boolean result = validator.isValid("Duplicate Title", null);

    assertFalse(result);
  }
}
