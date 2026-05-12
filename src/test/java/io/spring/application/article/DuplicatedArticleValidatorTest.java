package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.util.Collections;
import java.util.Optional;
import javax.validation.ConstraintValidatorContext;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DuplicatedArticleValidatorTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private ConstraintValidatorContext context;

  @InjectMocks private DuplicatedArticleValidator validator;

  @Test
  void should_return_true_when_title_is_unique() {
    when(articleQueryService.findBySlug("unique-title", null)).thenReturn(Optional.empty());

    assertTrue(validator.isValid("Unique Title", context));
  }

  @Test
  void should_return_false_when_title_already_exists() {
    ArticleData existing =
        new ArticleData(
            "id",
            "existing-title",
            "Existing Title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Collections.emptyList(),
            new ProfileData("uid", "user", "", "", false));
    when(articleQueryService.findBySlug("existing-title", null)).thenReturn(Optional.of(existing));

    assertFalse(validator.isValid("Existing Title", context));
  }
}
