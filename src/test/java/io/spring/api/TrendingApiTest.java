package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.readservice.StatsReadService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

  @MockBean private StatsReadService statsReadService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    List<TrendingArticleData> trending =
        Arrays.asList(
            new TrendingArticleData(
                "id1", "popular-article", "Popular Article", "Very popular", 10),
            new TrendingArticleData(
                "id2", "another-popular", "Another Popular", "Also popular", 8));

    when(statsReadService.getTrendingArticles(eq(7), eq(10))).thenReturn(trending);

    given()
        .when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(2))
        .body("articlesCount", equalTo(2))
        .body("articles[0].slug", equalTo("popular-article"))
        .body("articles[0].favoriteCount", equalTo(10))
        .body("articles[1].slug", equalTo("another-popular"))
        .body("articles[1].favoriteCount", equalTo(8));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(statsReadService.getTrendingArticles(eq(7), eq(10))).thenReturn(new ArrayList<>());

    given()
        .when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0))
        .body("articlesCount", equalTo(0));
  }

  @Test
  public void should_return_trending_articles_without_authentication() throws Exception {
    List<TrendingArticleData> trending =
        Arrays.asList(new TrendingArticleData("id1", "top-article", "Top Article", "The best", 15));

    when(statsReadService.getTrendingArticles(eq(7), eq(10))).thenReturn(trending);

    given()
        .when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(1))
        .body("articles[0].title", equalTo("Top Article"));
  }
}
