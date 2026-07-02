package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatsQueryService {
  private ArticleStatsReadService articleStatsReadService;

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    ArticleStatsData stats = articleStatsReadService.findStatsBySlug(slug);
    if (stats == null) {
      return Optional.empty();
    }
    return Optional.of(stats);
  }

  public List<TrendingArticleData> getTrendingArticles() {
    return articleStatsReadService.findTrendingArticles();
  }
}
