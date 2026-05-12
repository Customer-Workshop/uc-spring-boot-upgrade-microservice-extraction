package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ArticleStatsQueryService {

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    return Optional.empty();
  }

  public List<TrendingArticleData> getTrendingArticles() {
    return java.util.Collections.emptyList();
  }
}
