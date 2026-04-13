package io.spring.api;

import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.TrendingArticleData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/stats")
@AllArgsConstructor
public class TrendingApi {
  private ArticleStatsQueryService articleStatsQueryService;

  @GetMapping(path = "trending")
  public ResponseEntity<?> getTrending() {
    List<TrendingArticleData> trending = articleStatsQueryService.getTrendingArticles(7, 10);
    return ResponseEntity.ok(trendingResponse(trending));
  }

  private Map<String, Object> trendingResponse(List<TrendingArticleData> trending) {
    return new HashMap<String, Object>() {
      {
        put("articles", trending);
        put("articlesCount", trending.size());
      }
    };
  }
}
