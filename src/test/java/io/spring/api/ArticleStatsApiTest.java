package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

  @MockBean private ArticleStatsReadService articleStatsReadService;

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
    when(articleStatsReadService.countViewsByArticleId(eq(article.getId()))).thenReturn(10);
    when(articleStatsReadService.countFavoritesByArticleId(eq(article.getId()))).thenReturn(3);
    when(articleStatsReadService.countCommentsByArticleId(eq(article.getId()))).thenReturn(7);

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.viewCount", equalTo(10))
        .body("stats.favoriteCount", equalTo(3))
        .body("stats.commentCount", equalTo(7))
        .body("stats.daysSincePublished", equalTo(5));
  }

  @Test
  public void should_return_404_when_article_not_found_for_stats() throws Exception {
    when(articleRepository.findBySlug(eq("not-exists"))).thenReturn(Optional.empty());

    RestAssuredMockMvc.when().get("/articles/{slug}/stats", "not-exists").then().statusCode(404);
  }

  @Test
  public void should_get_article_stats_with_zero_counts() throws Exception {
    String slug = "new-article";
    DateTime createdAt = new DateTime();
    Article article =
        new Article(
            "New Article", "Desc", "Body", Collections.emptyList(), user.getId(), createdAt);

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(articleStatsReadService.countViewsByArticleId(eq(article.getId()))).thenReturn(0);
    when(articleStatsReadService.countFavoritesByArticleId(eq(article.getId()))).thenReturn(0);
    when(articleStatsReadService.countCommentsByArticleId(eq(article.getId()))).thenReturn(0);

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
    List<TrendingArticleData> trending =
        Arrays.asList(
            new TrendingArticleData("popular-article", "Popular Article", "A popular one", 15),
            new TrendingArticleData("another-hit", "Another Hit", "Another popular article", 10));

    when(articleStatsReadService.findTrendingArticles(eq(7))).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles.size()", equalTo(2))
        .body("articles[0].slug", equalTo("popular-article"))
        .body("articles[0].title", equalTo("Popular Article"))
        .body("articles[0].favoriteCount", equalTo(15))
        .body("articles[1].slug", equalTo("another-hit"))
        .body("articles[1].favoriteCount", equalTo(10));
  }

  @Test
  public void should_get_empty_trending_articles() throws Exception {
    when(articleStatsReadService.findTrendingArticles(eq(7))).thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles.size()", equalTo(0));
  }

  @Test
  public void should_access_stats_without_authentication() throws Exception {
    String slug = "test-article";
    DateTime createdAt = new DateTime().minusDays(2);
    Article article =
        new Article("Test Article", "Desc", "Body", Arrays.asList("java"), user.getId(), createdAt);

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(articleStatsReadService.countViewsByArticleId(eq(article.getId()))).thenReturn(0);
    when(articleStatsReadService.countFavoritesByArticleId(eq(article.getId()))).thenReturn(0);
    when(articleStatsReadService.countCommentsByArticleId(eq(article.getId()))).thenReturn(0);

    given().when().get("/articles/{slug}/stats", slug).then().statusCode(200);
  }

  @Test
  public void should_access_trending_without_authentication() throws Exception {
    when(articleStatsReadService.findTrendingArticles(eq(7))).thenReturn(Collections.emptyList());

    given().when().get("/stats/trending").then().statusCode(200);
  }
}
