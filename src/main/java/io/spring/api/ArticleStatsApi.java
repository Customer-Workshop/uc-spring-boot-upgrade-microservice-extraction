package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
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
  private ArticleStatsReadService articleStatsReadService;

  @GetMapping("/articles/{slug}/stats")
  public ResponseEntity<?> getArticleStats(@PathVariable("slug") String slug) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    int viewCount = articleStatsReadService.getViewCount(article.getId());
    int favoriteCount = articleStatsReadService.getFavoriteCount(article.getId());
    int commentCount = articleStatsReadService.getCommentCount(article.getId());
    long daysSincePublished = Days.daysBetween(article.getCreatedAt(), new DateTime()).getDays();

    ArticleStatsData stats =
        new ArticleStatsData(viewCount, favoriteCount, commentCount, daysSincePublished);

    Map<String, Object> response = new HashMap<>();
    response.put("stats", stats);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/stats/trending")
  public ResponseEntity<?> getTrendingArticles() {
    List<TrendingArticleData> trending = articleStatsReadService.findTrendingArticles(7, 10);

    Map<String, Object> response = new HashMap<>();
    response.put("articles", trending);
    response.put("articlesCount", trending.size());
    return ResponseEntity.ok(response);
  }
}
