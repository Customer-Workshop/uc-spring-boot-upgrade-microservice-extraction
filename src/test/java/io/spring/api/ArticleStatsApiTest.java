package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.StatsReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
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

  @MockBean private ArticleRepository articleRepository;

  @MockBean private StatsReadService statsReadService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    String slug = "test-article";
    DateTime createdAt = new DateTime().minusDays(5);
    Article article =
        new Article("Test Article", "Desc", "Body", Arrays.asList("java"), user.getId(), createdAt);

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(statsReadService.countViewsByArticleId(eq(article.getId()))).thenReturn(10);
    when(statsReadService.countFavoritesByArticleId(eq(article.getId()))).thenReturn(3);
    when(statsReadService.countCommentsByArticleId(eq(article.getId()))).thenReturn(7);

    given()
        .when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(10))
        .body("stats.favoriteCount", equalTo(3))
        .body("stats.commentCount", equalTo(7))
        .body("stats.daysSincePublished", equalTo(5));
  }

  @Test
  public void should_return_404_when_article_not_found_for_stats() throws Exception {
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    given().when().get("/articles/{slug}/stats", "non-existent").then().statusCode(404);
  }

  @Test
  public void should_get_article_stats_with_zero_counts() throws Exception {
    String slug = "new-article";
    DateTime createdAt = new DateTime();
    Article article =
        new Article(
            "New Article", "Desc", "Body", Collections.emptyList(), user.getId(), createdAt);

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(statsReadService.countViewsByArticleId(eq(article.getId()))).thenReturn(0);
    when(statsReadService.countFavoritesByArticleId(eq(article.getId()))).thenReturn(0);
    when(statsReadService.countCommentsByArticleId(eq(article.getId()))).thenReturn(0);

    given()
        .when()
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
    TrendingArticleData article1 = new TrendingArticleData("slug-1", "Title 1", "Desc 1", 15);
    TrendingArticleData article2 = new TrendingArticleData("slug-2", "Title 2", "Desc 2", 10);

    when(statsReadService.findTrendingArticles(anyString()))
        .thenReturn(Arrays.asList(article1, article2));

    given()
        .when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(2))
        .body("articles[0].slug", equalTo("slug-1"))
        .body("articles[0].title", equalTo("Title 1"))
        .body("articles[0].favoriteCount", equalTo(15))
        .body("articles[1].slug", equalTo("slug-2"))
        .body("articles[1].favoriteCount", equalTo(10));
  }

  @Test
  public void should_get_empty_trending_articles() throws Exception {
    when(statsReadService.findTrendingArticles(anyString())).thenReturn(Collections.emptyList());

    given().when().get("/stats/trending").then().statusCode(200).body("articles", hasSize(0));
  }
}
