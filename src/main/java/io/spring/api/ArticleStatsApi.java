package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ArticleStatsData;
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
public class ArticleStatsApi {
  private ArticleStatsQueryService articleStatsQueryService;

  @GetMapping("/articles/{slug}/stats")
  public ResponseEntity<?> getArticleStats(@PathVariable("slug") String slug) {
    return articleStatsQueryService
        .getArticleStats(slug)
        .map(stats -> ResponseEntity.ok(statsResponse(stats)))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @GetMapping("/stats/trending")
  public ResponseEntity<?> getTrendingArticles() {
    List<TrendingArticleData> trending = articleStatsQueryService.getTrendingArticles();
    return ResponseEntity.ok(trendingResponse(trending));
  }

  private Map<String, Object> statsResponse(ArticleStatsData stats) {
    return new HashMap<String, Object>() {
      {
        put("stats", stats);
      }
    };
  }

  private Map<String, Object> trendingResponse(List<TrendingArticleData> trending) {
    return new HashMap<String, Object>() {
      {
        put("articles", trending);
      }
    };
  }
}
