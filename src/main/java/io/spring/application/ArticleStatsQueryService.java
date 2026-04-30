package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatsQueryService {
  private ArticleStatsReadService articleStatsReadService;

  public ArticleStatsData getArticleStats(Article article) {
    String articleId = article.getId();
    Integer viewCountResult = articleStatsReadService.getViewCount(articleId);
    int viewCount = viewCountResult != null ? viewCountResult : 0;
    int favoriteCount = articleStatsReadService.getFavoriteCount(articleId);
    int commentCount = articleStatsReadService.getCommentCount(articleId);
    long daysSincePublished = Days.daysBetween(article.getCreatedAt(), new DateTime()).getDays();
    return new ArticleStatsData(
        article.getSlug(), viewCount, favoriteCount, commentCount, daysSincePublished);
  }

  public List<TrendingArticleData> getTrendingArticles() {
    return articleStatsReadService.findTrendingArticles(7, 10);
  }
}
