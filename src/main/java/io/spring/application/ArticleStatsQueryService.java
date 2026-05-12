package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatsQueryService {
  private ArticleRepository articleRepository;
  private ArticleStatsReadService articleStatsReadService;

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    Optional<Article> articleOpt = articleRepository.findBySlug(slug);
    if (!articleOpt.isPresent()) {
      return Optional.empty();
    }
    Article article = articleOpt.get();
    int viewCount = articleStatsReadService.countArticleViews(article.getId());
    int favoriteCount = articleStatsReadService.countArticleFavorites(article.getId());
    int commentCount = articleStatsReadService.countArticleComments(article.getId());
    long daysSincePublished = Days.daysBetween(article.getCreatedAt(), new DateTime()).getDays();
    return Optional.of(
        new ArticleStatsData(viewCount, favoriteCount, commentCount, daysSincePublished));
  }

  public List<TrendingArticleData> getTrendingArticles() {
    return articleStatsReadService.findTrendingArticles();
  }
}
