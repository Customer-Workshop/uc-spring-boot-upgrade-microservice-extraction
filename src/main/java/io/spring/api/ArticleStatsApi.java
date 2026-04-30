package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.ArticleRepository;
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
  private ArticleRepository articleRepository;

  @GetMapping(path = "/articles/{slug}/stats")
  public ResponseEntity<?> getArticleStats(@PathVariable("slug") String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              ArticleStatsData stats = articleStatsQueryService.getArticleStats(article);
              Map<String, Object> response = new HashMap<>();
              response.put("stats", stats);
              return ResponseEntity.ok(response);
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @GetMapping(path = "/stats/trending")
  public ResponseEntity<?> getTrendingArticles() {
    List<TrendingArticleData> trending = articleStatsQueryService.getTrendingArticles();
    Map<String, Object> response = new HashMap<>();
    response.put("articles", trending);
    return ResponseEntity.ok(response);
  }
}
