package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.data.ArticleStatsData;
import io.spring.infrastructure.mybatis.readservice.StatsReadService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/stats")
@AllArgsConstructor
public class ArticleStatsApi {
  private StatsReadService statsReadService;

  @GetMapping
  public ResponseEntity<?> getArticleStats(@PathVariable("slug") String slug) {
    Map<String, Object> raw = statsReadService.getArticleStatsBySlug(slug);
    if (raw == null) {
      throw new ResourceNotFoundException();
    }

    ArticleStatsData stats =
        new ArticleStatsData(
            (String) raw.get("slug"),
            (String) raw.get("title"),
            ((Number) raw.get("viewCount")).intValue(),
            ((Number) raw.get("favoriteCount")).intValue(),
            ((Number) raw.get("commentCount")).intValue(),
            ((Number) raw.get("daysSincePublished")).longValue());

    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("stats", stats);
          }
        });
  }
}
