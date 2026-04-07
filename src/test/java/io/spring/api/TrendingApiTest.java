package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.TrendingArticleData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({TrendingApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class TrendingApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatsQueryService articleStatsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_trending_articles() throws Exception {
    DateTime now = new DateTime();
    List<TrendingArticleData> trending =
        Arrays.asList(
            new TrendingArticleData("id1", "popular-article", "Popular Article", "desc1", 15, now),
            new TrendingArticleData(
                "id2", "another-popular", "Another Popular", "desc2", 10, now));

    when(articleStatsQueryService.getTrendingArticles()).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(2))
        .body("articlesCount", equalTo(2))
        .body("articles[0].slug", equalTo("popular-article"))
        .body("articles[0].favoriteCount", equalTo(15))
        .body("articles[1].slug", equalTo("another-popular"))
        .body("articles[1].favoriteCount", equalTo(10));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(articleStatsQueryService.getTrendingArticles()).thenReturn(new ArrayList<>());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0))
        .body("articlesCount", equalTo(0));
  }

  @Test
  public void should_get_trending_articles_without_auth() throws Exception {
    DateTime now = new DateTime();
    List<TrendingArticleData> trending =
        Arrays.asList(
            new TrendingArticleData("id1", "hot-article", "Hot Article", "desc", 20, now));

    when(articleStatsQueryService.getTrendingArticles()).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(1))
        .body("articles[0].title", equalTo("Hot Article"));
  }
}
