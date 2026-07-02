package io.spring.application;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleStatsData;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ArticleStatsQueryService {

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  public List<ArticleData> getTrendingArticles() {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
