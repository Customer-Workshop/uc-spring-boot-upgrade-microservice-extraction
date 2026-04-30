package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.ArticleStatisticsData;
import io.spring.application.data.TrendingArticleData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ArticleStatisticsApi {
  private ArticleStatisticsQueryService articleStatisticsQueryService;

  @GetMapping("/articles/{slug}/stats")
  public ResponseEntity<Map<String, Object>> getArticleStats(@PathVariable("slug") String slug) {
    ArticleStatisticsData stats =
        articleStatisticsQueryService
            .getArticleStatsBySlug(slug)
            .orElseThrow(ResourceNotFoundException::new);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("stats", stats);
          }
        });
  }

  @GetMapping("/stats/trending")
  public ResponseEntity<Map<String, Object>> getTrendingArticles() {
    List<TrendingArticleData> trending = articleStatisticsQueryService.getTrendingArticles();
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("articles", trending);
            put("articlesCount", trending.size());
          }
        });
  }
}
