package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatsQueryServiceImpl implements ArticleStatsQueryService {
  private ArticleStatsReadService articleStatsReadService;
  private ArticleReadService articleReadService;

  @Override
  public ArticleStatsData getArticleStats(String articleId) {
    int favoriteCount = articleStatsReadService.countFavoritesByArticleId(articleId);
    int commentCount = articleStatsReadService.countCommentsByArticleId(articleId);

    var articleData = articleReadService.findById(articleId);
    int daysSincePublished = 0;
    if (articleData != null && articleData.getCreatedAt() != null) {
      daysSincePublished =
          Days.daysBetween(articleData.getCreatedAt().toLocalDate(), DateTime.now().toLocalDate())
              .getDays();
    }

    return new ArticleStatsData(0, favoriteCount, commentCount, daysSincePublished);
  }

  @Override
  public List<TrendingArticleData> getTrendingArticles() {
    return articleStatsReadService.findTrendingArticles();
  }
}
