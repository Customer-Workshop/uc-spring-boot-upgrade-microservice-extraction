package io.spring.articleservice.application.article;

import io.spring.articleservice.application.ArticleQueryService;
import io.spring.articleservice.core.article.Article;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

class DuplicatedArticleValidator
    implements ConstraintValidator<DuplicatedArticleConstraint, String> {

  @Autowired private ArticleQueryService articleQueryService;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true;
    }
    return !articleQueryService.findBySlug(Article.toSlug(value), null).isPresent();
  }
}
