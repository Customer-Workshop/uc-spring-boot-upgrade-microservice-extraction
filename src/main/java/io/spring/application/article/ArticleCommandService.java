package io.spring.application.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/** Creates and updates articles through the article repository. */
@Service
@Validated
@AllArgsConstructor
public class ArticleCommandService {

  private ArticleRepository articleRepository;

  /**
   * Creates an article from the supplied fields, associates it with the creator, and saves it.
   *
   * @param newArticleParam validated article fields
   * @param creator user who owns the article
   * @return the newly created article
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
   * Applies non-empty update fields to an article and saves it.
   *
   * @param article article to update
   * @param updateArticleParam validated update fields
   * @return the updated article
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
