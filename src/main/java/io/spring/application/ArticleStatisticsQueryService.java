package io.spring.application;

import io.spring.application.data.ArticleStatisticsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.ArticleStatisticsReadService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatisticsQueryService {
  private static final DateTimeFormatter SQLITE_FORMAT =
      DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

  private ArticleRepository articleRepository;
  private ArticleStatisticsReadService articleStatisticsReadService;

  public Optional<ArticleStatisticsData> getArticleStatsBySlug(String slug) {
    Optional<Article> articleOpt = articleRepository.findBySlug(slug);
    if (articleOpt.isEmpty()) {
      return Optional.empty();
    }
    Article article = articleOpt.get();
    int favoriteCount = articleStatisticsReadService.countFavoritesByArticleId(article.getId());
    int commentCount = articleStatisticsReadService.countCommentsByArticleId(article.getId());
    long daysSincePublished =
        Days.daysBetween(article.getCreatedAt().toLocalDate(), DateTime.now().toLocalDate())
            .getDays();

    return Optional.of(
        new ArticleStatisticsData(
            article.getSlug(), 0, favoriteCount, commentCount, daysSincePublished));
  }

  public List<TrendingArticleData> getTrendingArticles() {
    DateTime sevenDaysAgo = DateTime.now().minusDays(7);
    String since = SQLITE_FORMAT.print(sevenDaysAgo);
    return articleStatisticsReadService.findTrendingArticles(since, 10);
  }
}
