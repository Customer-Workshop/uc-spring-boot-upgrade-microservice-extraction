package io.spring.application.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Write-side service for article create and update operations.
 *
 * <p>Validates input parameters and delegates persistence to {@link ArticleRepository}.
 */
@Service
@Validated
@AllArgsConstructor
public class ArticleCommandService {

  private ArticleRepository articleRepository;

  /**
   * Creates a new article with the given parameters.
   *
   * @param newArticleParam validated article creation parameters (title, description, body, tags)
   * @param creator the user authoring the article
   * @return the newly created article entity
   */
  public Article createArticle(@Valid NewArticleParam newArticleParam, User creator) {
    Article article =
        new Article(
            newArticleParam.getTitle(),
            newArticleParam.getDescription(),
            newArticleParam.getBody(),
            newArticleParam.getTagList(),
            creator.getId());
    articleRepository.save(article);
    return article;
  }

  /**
   * Updates an existing article's title, description, and/or body.
   *
   * @param article the article entity to update
   * @param updateArticleParam validated update parameters; empty strings are ignored
   * @return the updated article entity
   */
  public Article updateArticle(Article article, @Valid UpdateArticleParam updateArticleParam) {
    article.update(
        updateArticleParam.getTitle(),
        updateArticleParam.getDescription(),
        updateArticleParam.getBody());
    articleRepository.save(article);
    return article;
  }
}
