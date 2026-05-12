package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatsQueryService {
  private ArticleRepository articleRepository;
  private ArticleStatsReadService articleStatsReadService;

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              int favoriteCount =
                  articleStatsReadService.countFavoritesByArticleId(article.getId());
              int commentCount = articleStatsReadService.countCommentsByArticleId(article.getId());
              long daysSincePublished =
                  Days.daysBetween(article.getCreatedAt(), new DateTime()).getDays();
              return new ArticleStatsData(
                  article.getSlug(),
                  article.getTitle(),
                  0,
                  favoriteCount,
                  commentCount,
                  daysSincePublished);
            });
  }

  public List<TrendingArticleData> getTrendingArticles() {
    return articleStatsReadService.findTrendingArticles(10);
  }
}
