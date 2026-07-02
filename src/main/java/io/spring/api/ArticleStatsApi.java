package io.spring.api;

import io.spring.application.ArticleStatsQueryService;
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
    // TODO: implement - tests should fail
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/stats/trending")
  public ResponseEntity<?> getTrendingArticles() {
    // TODO: implement - tests should fail
    return ResponseEntity.ok().build();
  }
}
