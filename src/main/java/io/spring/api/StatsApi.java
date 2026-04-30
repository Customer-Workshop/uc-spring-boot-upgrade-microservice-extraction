package io.spring.api;

import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.StatsReadService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/stats")
@AllArgsConstructor
public class StatsApi {
  private StatsReadService statsReadService;
  private ArticleReadService articleReadService;
  private ArticleQueryService articleQueryService;

  @GetMapping("/trending")
  public ResponseEntity<?> getTrendingArticles(@AuthenticationPrincipal User user) {
    List<String> articleIds = statsReadService.findTrendingArticleIds(10);
    if (articleIds.isEmpty()) {
      return ResponseEntity.ok(trendingResponse(new ArrayList<>()));
    }
    List<ArticleData> articles = articleReadService.findArticles(articleIds);
    return ResponseEntity.ok(trendingResponse(articles));
  }

  private Map<String, Object> trendingResponse(List<ArticleData> articles) {
    return new HashMap<String, Object>() {
      {
        put("articles", articles);
        put("articlesCount", articles.size());
      }
    };
  }
}
