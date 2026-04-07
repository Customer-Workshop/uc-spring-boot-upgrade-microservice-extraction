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
import org.joda.time.format.ISODateTimeFormat;
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
    int favoriteCount = articleStatsReadService.countFavoritesByArticleId(article.getId());
    int commentCount = articleStatsReadService.countCommentsByArticleId(article.getId());
    long daysSincePublished =
        Days.daysBetween(article.getCreatedAt().toLocalDate(), new DateTime().toLocalDate())
            .getDays();

    ArticleStatsData stats = new ArticleStatsData();
    stats.setSlug(article.getSlug());
    stats.setViewCount(0);
    stats.setFavoriteCount(favoriteCount);
    stats.setCommentCount(commentCount);
    stats.setDaysSincePublished(daysSincePublished);
    return Optional.of(stats);
  }

  public List<TrendingArticleData> getTrendingArticles() {
    DateTime since = new DateTime().minusDays(7);
    String sinceStr = since.toString(ISODateTimeFormat.dateTime());
    return articleStatsReadService.findTrendingArticles(sinceStr, 10);
  }
}
