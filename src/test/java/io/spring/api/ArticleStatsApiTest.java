package io.spring.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.TestHelper;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleStatsData;
import io.spring.core.user.User;
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
    ArticleStatsData statsData = new ArticleStatsData(150, 42, 7, 10);

    when(articleStatsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("articleStats.viewCount", equalTo(150))
        .body("articleStats.favoriteCount", equalTo(42))
        .body("articleStats.commentCount", equalTo(7))
        .body("articleStats.daysSincePublished", equalTo(10));
  }

  @Test
  public void should_get_404_if_article_not_found_for_stats() throws Exception {
    when(articleStatsQueryService.getArticleStats(eq("non-existent"))).thenReturn(Optional.empty());

    RestAssuredMockMvc.when().get("/articles/{slug}/stats", "non-existent").then().statusCode(404);
  }

  @Test
  public void should_get_article_stats_with_zero_counts() throws Exception {
    String slug = "brand-new-article";
    ArticleStatsData statsData = new ArticleStatsData(0, 0, 0, 0);

    when(articleStatsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("articleStats.viewCount", equalTo(0))
        .body("articleStats.favoriteCount", equalTo(0))
        .body("articleStats.commentCount", equalTo(0))
        .body("articleStats.daysSincePublished", equalTo(0));
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    User author = new User("author@test.com", "author", "123", "", "");
    ArticleData article1 = TestHelper.articleDataFixture("1", author);
    ArticleData article2 = TestHelper.articleDataFixture("2", author);
    ArticleData article3 = TestHelper.articleDataFixture("3", author);
    List<ArticleData> trending = Arrays.asList(article1, article2, article3);

    when(articleStatsQueryService.getTrendingArticles()).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(3))
        .body("articlesCount", equalTo(3))
        .body("articles[0].slug", equalTo(article1.getSlug()))
        .body("articles[1].slug", equalTo(article2.getSlug()))
        .body("articles[2].slug", equalTo(article3.getSlug()));
  }

  @Test
  public void should_get_empty_trending_when_no_articles() throws Exception {
    when(articleStatsQueryService.getTrendingArticles()).thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0))
        .body("articlesCount", equalTo(0));
  }

  @Test
  public void should_get_trending_articles_without_auth() throws Exception {
    User author = new User("author@test.com", "author", "123", "", "");
    ArticleData article1 = TestHelper.articleDataFixture("trending", author);
    List<ArticleData> trending = Collections.singletonList(article1);

    when(articleStatsQueryService.getTrendingArticles()).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(1));
  }

  @Test
  public void should_get_article_stats_without_auth() throws Exception {
    String slug = "public-article";
    ArticleStatsData statsData = new ArticleStatsData(500, 100, 25, 30);

    when(articleStatsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("articleStats.viewCount", equalTo(500))
        .body("articleStats.favoriteCount", equalTo(100))
        .body("articleStats.commentCount", equalTo(25))
        .body("articleStats.daysSincePublished", equalTo(30));
  }
}
