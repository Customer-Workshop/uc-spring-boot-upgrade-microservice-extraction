package io.spring.application;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.ArticleStatsData;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatsQueryService {
  private ArticleReadService articleReadService;
  private ArticleFavoritesReadService articleFavoritesReadService;
  private ArticleStatsReadService articleStatsReadService;

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }
    int favoriteCount = articleFavoritesReadService.articleFavoriteCount(articleData.getId());
    int commentCount = articleStatsReadService.countCommentsByArticleId(articleData.getId());
    int viewCount = articleStatsReadService.countViewsByArticleId(articleData.getId());
    long daysSincePublished =
        Days.daysBetween(articleData.getCreatedAt(), new DateTime()).getDays();
    return Optional.of(
        new ArticleStatsData(viewCount, favoriteCount, commentCount, daysSincePublished));
  }

  public List<ArticleData> getTrendingArticles() {
    DateTime sevenDaysAgo = new DateTime().minusDays(7);
    List<ArticleData> articles = articleStatsReadService.findTrendingArticles(sevenDaysAgo, 10);
    if (articles.isEmpty()) {
      return Collections.emptyList();
    }
    setFavoriteCount(articles);
    return articles;
  }

  private void setFavoriteCount(List<ArticleData> articles) {
    List<ArticleFavoriteCount> favoritesCounts =
        articleFavoritesReadService.articlesFavoriteCount(
            articles.stream().map(ArticleData::getId).collect(Collectors.toList()));
    Map<String, Integer> countMap = new HashMap<>();
    favoritesCounts.forEach(item -> countMap.put(item.getId(), item.getCount()));
    articles.forEach(
        articleData -> articleData.setFavoritesCount(countMap.get(articleData.getId())));
  }
}
