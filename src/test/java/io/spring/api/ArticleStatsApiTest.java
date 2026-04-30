package io.spring.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticleStatsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleStatsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatsQueryService articleStatsQueryService;

  @MockBean private ArticleRepository articleRepository;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    String slug = "test-article";
    Article article =
        new Article("Test Article", "Desc", "Body", Arrays.asList("java"), user.getId());

    ArticleStatsData stats = new ArticleStatsData(slug, 10, 5, 3, 7);

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(articleStatsQueryService.getArticleStats(eq(article))).thenReturn(stats);

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.viewCount", equalTo(10))
        .body("stats.favoriteCount", equalTo(5))
        .body("stats.commentCount", equalTo(3))
        .body("stats.daysSincePublished", equalTo(7));
  }

  @Test
  public void should_return_404_when_article_not_found_for_stats() throws Exception {
    when(articleRepository.findBySlug(eq("non-existent"))).thenReturn(Optional.empty());

    RestAssuredMockMvc.when().get("/articles/{slug}/stats", "non-existent").then().statusCode(404);
  }

  @Test
  public void should_get_article_stats_with_zero_counts() throws Exception {
    String slug = "new-article";
    Article article =
        new Article("New Article", "Desc", "Body", Collections.emptyList(), user.getId());

    ArticleStatsData stats = new ArticleStatsData(slug, 0, 0, 0, 0);

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(articleStatsQueryService.getArticleStats(eq(article))).thenReturn(stats);

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(0))
        .body("stats.commentCount", equalTo(0))
        .body("stats.daysSincePublished", equalTo(0));
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    TrendingArticleData article1 =
        new TrendingArticleData("popular-article", "Popular Article", "A popular article", 25);
    TrendingArticleData article2 =
        new TrendingArticleData(
            "another-popular", "Another Popular", "Another popular article", 15);

    when(articleStatsQueryService.getTrendingArticles())
        .thenReturn(Arrays.asList(article1, article2));

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(2))
        .body("articles[0].slug", equalTo("popular-article"))
        .body("articles[0].favoriteCount", equalTo(25))
        .body("articles[1].slug", equalTo("another-popular"))
        .body("articles[1].favoriteCount", equalTo(15));
  }

  @Test
  public void should_get_empty_trending_articles() throws Exception {
    when(articleStatsQueryService.getTrendingArticles()).thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0));
  }
}
