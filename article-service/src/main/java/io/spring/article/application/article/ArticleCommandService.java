package io.spring.article.application.article;

import io.spring.article.core.article.Article;
import io.spring.article.core.article.ArticleRepository;
import io.spring.article.infrastructure.client.UserProfileCacheService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleCommandService {
  private ArticleRepository articleRepository;
  private UserProfileCacheService userProfileCacheService;

  public Article createArticle(NewArticleParam newArticleParam, String userId) {
    userProfileCacheService.ensureProfileCached(userId);
    Article article =
        new Article(
            newArticleParam.getTitle(),
            newArticleParam.getDescription(),
            newArticleParam.getBody(),
            newArticleParam.getTagList(),
            userId);
    articleRepository.save(article);
    return article;
  }

  public Article updateArticle(Article article, UpdateArticleParam updateArticleParam) {
    article.update(
        updateArticleParam.getTitle(),
        updateArticleParam.getDescription(),
        updateArticleParam.getBody());
    articleRepository.save(article);
    return article;
  }
}
