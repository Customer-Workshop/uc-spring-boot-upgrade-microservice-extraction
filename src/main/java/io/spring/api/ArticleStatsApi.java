package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleStatsService;
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
  private ArticleStatsService articleStatsService;

  @GetMapping
  public ResponseEntity<?> getArticleStats(@PathVariable("slug") String slug) {
    return articleStatsService
        .getArticleStats(slug)
        .map(
            stats -> {
              Map<String, Object> response = new HashMap<>();
              response.put("stats", stats);
              return ResponseEntity.ok(response);
            })
        .orElseThrow(ResourceNotFoundException::new);
  }
}
