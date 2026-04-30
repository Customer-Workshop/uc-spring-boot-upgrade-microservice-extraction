package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.mapper.ArticleViewsMapper;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
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
  private ArticleReadService articleReadService;
  private ArticleStatsReadService articleStatsReadService;
  private ArticleViewsMapper articleViewsMapper;

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    var articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }

    String articleId = articleData.getId();

    articleViewsMapper.ensureViewRecord(articleId);
    articleViewsMapper.incrementViewCount(articleId);

    int viewCount = articleStatsReadService.getViewCount(articleId);
    int favoriteCount = articleStatsReadService.getFavoriteCount(articleId);
    int commentCount = articleStatsReadService.getCommentCount(articleId);
    long daysSincePublished =
        Days.daysBetween(articleData.getCreatedAt(), new DateTime()).getDays();

    return Optional.of(
        new ArticleStatsData(slug, viewCount, favoriteCount, commentCount, daysSincePublished));
  }

  public List<TrendingArticleData> getTrendingArticles() {
    return articleStatsReadService.findTrendingArticles(10);
  }
}
