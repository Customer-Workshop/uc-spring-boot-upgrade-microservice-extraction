package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.infrastructure.mybatis.readservice.StatsReadService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatsQueryService {
  private StatsReadService statsReadService;

  public ArticleStatsData getArticleStats(Article article) {
    String articleId = article.getId();
    statsReadService.incrementViewCount(articleId);
    int viewCount = statsReadService.getViewCount(articleId);
    int favoriteCount = statsReadService.getFavoriteCount(articleId);
    int commentCount = statsReadService.getCommentCount(articleId);
    long daysSincePublished = Days.daysBetween(article.getCreatedAt(), new DateTime()).getDays();
    return new ArticleStatsData(
        article.getSlug(),
        article.getTitle(),
        viewCount,
        favoriteCount,
        commentCount,
        daysSincePublished);
  }

  public List<TrendingArticleData> getTrendingArticles(int days, int limit) {
    return statsReadService.findTrendingArticles(days, limit);
  }
}
