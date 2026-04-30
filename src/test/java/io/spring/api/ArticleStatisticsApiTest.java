package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.ArticleStatisticsData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.TrendingArticleData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticleStatisticsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleStatisticsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatisticsQueryService articleStatisticsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    String slug = "test-article";
    ArticleStatisticsData stats = new ArticleStatisticsData(slug, 0, 5, 3, 10);

    when(articleStatisticsQueryService.getArticleStatsBySlug(eq(slug)))
        .thenReturn(Optional.of(stats));

    given()
        .when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(5))
        .body("stats.commentCount", equalTo(3))
        .body("stats.daysSincePublished", equalTo(10));
  }

  @Test
  public void should_404_if_article_not_found_for_stats() throws Exception {
    when(articleStatisticsQueryService.getArticleStatsBySlug(anyString()))
        .thenReturn(Optional.empty());

    given().when().get("/articles/{slug}/stats", "non-existent").then().statusCode(404);
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    ProfileData author = new ProfileData("user1", "johndoe", "bio", "image.jpg", false);
    TrendingArticleData trending1 =
        new TrendingArticleData("popular-article", "Popular Article", "desc1", 10, author);
    TrendingArticleData trending2 =
        new TrendingArticleData("another-popular", "Another Popular", "desc2", 8, author);
    List<TrendingArticleData> trendingList = Arrays.asList(trending1, trending2);

    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(trendingList);

    given()
        .when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(2))
        .body("articles[0].slug", equalTo("popular-article"))
        .body("articles[0].favoriteCount", equalTo(10))
        .body("articles[0].title", equalTo("Popular Article"))
        .body("articles[0].author.username", equalTo("johndoe"))
        .body("articles[1].slug", equalTo("another-popular"))
        .body("articles[1].favoriteCount", equalTo(8));
  }

  @Test
  public void should_get_empty_trending_articles() throws Exception {
    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(Collections.emptyList());

    given()
        .when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(0))
        .body("articles.size()", equalTo(0));
  }
}
