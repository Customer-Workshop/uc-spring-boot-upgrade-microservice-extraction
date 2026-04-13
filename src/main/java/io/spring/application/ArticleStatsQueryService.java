package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.StatsReadService;
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
  private StatsReadService statsReadService;

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    Optional<Article> articleOpt = articleRepository.findBySlug(slug);
    if (articleOpt.isEmpty()) {
      return Optional.empty();
    }
    Article article = articleOpt.get();
    int favoriteCount = statsReadService.countFavoritesByArticleId(article.getId());
    int commentCount = statsReadService.countCommentsByArticleId(article.getId());
    long daysSincePublished =
        Days.daysBetween(article.getCreatedAt().toLocalDate(), new DateTime().toLocalDate())
            .getDays();
    return Optional.of(
        new ArticleStatsData(
            article.getSlug(), 0, favoriteCount, commentCount, daysSincePublished));
  }

  public List<TrendingArticleData> getTrendingArticles(int days, int limit) {
    return statsReadService.findTrendingArticles(days, limit);
  }
}
