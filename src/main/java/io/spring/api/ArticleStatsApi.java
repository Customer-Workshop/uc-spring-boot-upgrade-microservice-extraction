package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ArticleData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@AllArgsConstructor
public class ArticleStatsApi {
  private ArticleStatsQueryService articleStatsQueryService;

  @GetMapping(path = "/articles/{slug}/stats")
  public ResponseEntity<?> getArticleStats(@PathVariable("slug") String slug) {
    return articleStatsQueryService
        .getArticleStats(slug)
        .map(
            statsData ->
                ResponseEntity.ok(
                    new HashMap<String, Object>() {
                      {
                        put("articleStats", statsData);
                      }
                    }))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @GetMapping(path = "/stats/trending")
  public ResponseEntity<Map<String, Object>> getTrendingArticles() {
    List<ArticleData> trending = articleStatsQueryService.getTrendingArticles();
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("articles", trending);
            put("articlesCount", trending.size());
          }
        });
  }
}
