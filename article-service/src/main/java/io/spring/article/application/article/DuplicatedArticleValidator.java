package io.spring.article.application.article;

import io.spring.article.core.article.Article;
import io.spring.article.core.article.ArticleRepository;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class DuplicatedArticleValidator
    implements ConstraintValidator<DuplicatedArticleConstraint, NewArticleParam> {
  @Autowired private ArticleRepository articleRepository;

  @Override
  public boolean isValid(NewArticleParam value, ConstraintValidatorContext context) {
    return !articleRepository.findBySlug(Article.toSlug(value.getTitle())).isPresent();
  }
}
