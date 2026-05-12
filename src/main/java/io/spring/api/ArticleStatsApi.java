package io.spring.api;

import io.spring.application.ArticleStatsQueryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
    // TODO: implement article stats endpoint
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @GetMapping("/stats/trending")
  public ResponseEntity<?> getTrendingArticles() {
    // TODO: implement trending articles endpoint
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}
