package io.spring.application;

import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatsQueryService {
  private ArticleFavoritesReadService articleFavoritesReadService;
  private CommentReadService commentReadService;

  public Map<String, Object> getArticleStats(String articleId, DateTime createdAt) {
    int favoriteCount = articleFavoritesReadService.articleFavoriteCount(articleId);
    int commentCount = commentReadService.countByArticleId(articleId);
    int daysSincePublished = Days.daysBetween(createdAt.toLocalDate(), DateTime.now().toLocalDate()).getDays();

    Map<String, Object> stats = new HashMap<>();
    stats.put("favoriteCount", favoriteCount);
    stats.put("commentCount", commentCount);
    stats.put("daysSincePublished", daysSincePublished);

    Map<String, Object> result = new HashMap<>();
    result.put("stats", stats);
    return result;
  }
}
