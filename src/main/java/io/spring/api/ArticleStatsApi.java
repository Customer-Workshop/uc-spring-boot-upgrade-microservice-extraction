package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.data.ArticleStatsData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/stats")
@AllArgsConstructor
public class ArticleStatsApi {
  private ArticleRepository articleRepository;
  private ArticleStatsReadService articleStatsReadService;

  @GetMapping
  public ResponseEntity<?> getArticleStats(@PathVariable("slug") String slug) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    int viewCount = articleStatsReadService.countViewsByArticleId(article.getId());
    int favoriteCount = articleStatsReadService.countFavoritesByArticleId(article.getId());
    int commentCount = articleStatsReadService.countCommentsByArticleId(article.getId());
    long daysSincePublished = Days.daysBetween(article.getCreatedAt(), new DateTime()).getDays();

    ArticleStatsData stats =
        new ArticleStatsData(viewCount, favoriteCount, commentCount, daysSincePublished);

    return ResponseEntity.ok(statsResponse(stats));
  }

  private Map<String, Object> statsResponse(ArticleStatsData stats) {
    return new HashMap<String, Object>() {
      {
        put("stats", stats);
      }
    };
  }
}
