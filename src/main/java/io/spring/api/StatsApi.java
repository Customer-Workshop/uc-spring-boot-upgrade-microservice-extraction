package io.spring.api;

import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
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
public class StatsApi {
  private ArticleStatsReadService articleStatsReadService;

  @GetMapping(path = "/trending")
  public ResponseEntity<?> getTrendingArticles() {
    List<TrendingArticleData> trending = articleStatsReadService.findTrendingArticles(7, 10);

    return ResponseEntity.ok(trendingResponse(trending));
  }

  private Map<String, Object> trendingResponse(List<TrendingArticleData> articles) {
    return new HashMap<String, Object>() {
      {
        put("articles", articles);
        put("articlesCount", articles.size());
      }
    };
  }
}
