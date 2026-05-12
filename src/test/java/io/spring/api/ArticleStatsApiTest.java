package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.TrendingArticleData;
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

@WebMvcTest(ArticleStatsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleStatsApiTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatsQueryService articleStatsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    String slug = "test-article";
    ArticleStatsData statsData = new ArticleStatsData(slug, "Test Article", 0, 5, 3, 10);

    when(articleStatsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.title", equalTo("Test Article"))
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(5))
        .body("stats.commentCount", equalTo(3))
        .body("stats.daysSincePublished", equalTo(10));
  }

  @Test
  public void should_get_404_if_article_not_found_for_stats() throws Exception {
    when(articleStatsQueryService.getArticleStats(eq("non-existent"))).thenReturn(Optional.empty());

    RestAssuredMockMvc.when().get("/articles/{slug}/stats", "non-existent").then().statusCode(404);
  }

  @Test
  public void should_get_article_stats_with_zero_counts() throws Exception {
    String slug = "new-article";
    ArticleStatsData statsData = new ArticleStatsData(slug, "New Article", 0, 0, 0, 0);

    when(articleStatsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.favoriteCount", equalTo(0))
        .body("stats.commentCount", equalTo(0))
        .body("stats.daysSincePublished", equalTo(0));
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    ProfileData authorProfile = new ProfileData("userId", "johndoe", "bio", "image", false);
    TrendingArticleData article1 =
        new TrendingArticleData(
            "popular-article", "Popular Article", "A popular post", 100, authorProfile);
    TrendingArticleData article2 =
        new TrendingArticleData(
            "another-hit", "Another Hit", "Another popular post", 50, authorProfile);

    when(articleStatsQueryService.getTrendingArticles())
        .thenReturn(Arrays.asList(article1, article2));

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles[0].slug", equalTo("popular-article"))
        .body("articles[0].title", equalTo("Popular Article"))
        .body("articles[0].favoriteCount", equalTo(100))
        .body("articles[0].author.username", equalTo("johndoe"))
        .body("articles[1].slug", equalTo("another-hit"))
        .body("articles[1].favoriteCount", equalTo(50))
        .body("articlesCount", equalTo(2));
  }

  @Test
  public void should_get_empty_trending_when_no_articles() throws Exception {
    when(articleStatsQueryService.getTrendingArticles()).thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles.size()", equalTo(0))
        .body("articlesCount", equalTo(0));
  }

  @Test
  public void should_get_trending_articles_ordered_by_favorites() throws Exception {
    ProfileData authorProfile = new ProfileData("userId", "author1", "bio", "image", false);
    TrendingArticleData article1 =
        new TrendingArticleData("most-popular", "Most Popular", "desc1", 200, authorProfile);
    TrendingArticleData article2 =
        new TrendingArticleData("second-popular", "Second Popular", "desc2", 150, authorProfile);
    TrendingArticleData article3 =
        new TrendingArticleData("third-popular", "Third Popular", "desc3", 75, authorProfile);

    when(articleStatsQueryService.getTrendingArticles())
        .thenReturn(Arrays.asList(article1, article2, article3));

    given()
        .when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles[0].favoriteCount", equalTo(200))
        .body("articles[1].favoriteCount", equalTo(150))
        .body("articles[2].favoriteCount", equalTo(75))
        .body("articlesCount", equalTo(3));
  }
}
