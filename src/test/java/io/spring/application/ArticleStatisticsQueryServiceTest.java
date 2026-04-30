package io.spring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.data.ArticleStatisticsData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.ArticleStatisticsReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleStatisticsQueryServiceTest {

  @Mock private ArticleRepository articleRepository;

  @Mock private ArticleStatisticsReadService articleStatisticsReadService;

  private ArticleStatisticsQueryService queryService;

  @BeforeEach
  public void setUp() {
    queryService =
        new ArticleStatisticsQueryService(articleRepository, articleStatisticsReadService);
  }

  @Test
  public void should_return_stats_for_existing_article() {
    Article article =
        new Article(
            "Test Article",
            "description",
            "body",
            Collections.singletonList("java"),
            "user1",
            DateTime.now().minusDays(5));

    when(articleRepository.findBySlug(eq("test-article"))).thenReturn(Optional.of(article));
    when(articleStatisticsReadService.countFavoritesByArticleId(eq(article.getId()))).thenReturn(3);
    when(articleStatisticsReadService.countCommentsByArticleId(eq(article.getId()))).thenReturn(7);

    Optional<ArticleStatisticsData> result = queryService.getArticleStatsBySlug("test-article");

    assertTrue(result.isPresent());
    ArticleStatisticsData stats = result.get();
    assertEquals("test-article", stats.getSlug());
    assertEquals(0, stats.getViewCount());
    assertEquals(3, stats.getFavoriteCount());
    assertEquals(7, stats.getCommentCount());
    assertEquals(5, stats.getDaysSincePublished());
  }

  @Test
  public void should_return_empty_for_nonexistent_article() {
    when(articleRepository.findBySlug(eq("does-not-exist"))).thenReturn(Optional.empty());

    Optional<ArticleStatisticsData> result = queryService.getArticleStatsBySlug("does-not-exist");

    assertFalse(result.isPresent());
  }

  @Test
  public void should_return_trending_articles() {
    ProfileData author = new ProfileData("user1", "johndoe", "bio", "image.jpg", false);
    TrendingArticleData trending1 =
        new TrendingArticleData("popular", "Popular", "desc", 10, author);
    TrendingArticleData trending2 =
        new TrendingArticleData("another", "Another", "desc", 5, author);

    when(articleStatisticsReadService.findTrendingArticles(anyString(), eq(10)))
        .thenReturn(Arrays.asList(trending1, trending2));

    List<TrendingArticleData> result = queryService.getTrendingArticles();

    assertEquals(2, result.size());
    assertEquals("popular", result.get(0).getSlug());
    assertEquals(10, result.get(0).getFavoriteCount());
    assertEquals("another", result.get(1).getSlug());
    assertEquals(5, result.get(1).getFavoriteCount());
  }

  @Test
  public void should_return_empty_trending_when_no_articles() {
    when(articleStatisticsReadService.findTrendingArticles(anyString(), eq(10)))
        .thenReturn(Collections.emptyList());

    List<TrendingArticleData> result = queryService.getTrendingArticles();

    assertTrue(result.isEmpty());
  }
}
