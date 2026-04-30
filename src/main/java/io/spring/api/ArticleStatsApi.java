package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.StatsReadService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ArticleStatsApi {
  private ArticleRepository articleRepository;
  private StatsReadService statsReadService;

  @GetMapping("/articles/{slug}/stats")
  public ResponseEntity<?> getArticleStats(@PathVariable("slug") String slug) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    int viewCount = statsReadService.countViewsByArticleId(article.getId());
    int favoriteCount = statsReadService.countFavoritesByArticleId(article.getId());
    int commentCount = statsReadService.countCommentsByArticleId(article.getId());
    long daysSincePublished =
        Days.daysBetween(article.getCreatedAt().toLocalDate(), new DateTime().toLocalDate())
            .getDays();

    ArticleStatsData stats =
        new ArticleStatsData(viewCount, favoriteCount, commentCount, daysSincePublished);

    Map<String, Object> response = new HashMap<>();
    response.put("stats", stats);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/stats/trending")
  public ResponseEntity<?> getTrendingArticles() {
    String since = new DateTime().minusDays(7).toString("yyyy-MM-dd HH:mm:ss");
    List<TrendingArticleData> trending = statsReadService.findTrendingArticles(since);

    Map<String, Object> response = new HashMap<>();
    response.put("articles", trending);
    return ResponseEntity.ok(response);
  }
}
