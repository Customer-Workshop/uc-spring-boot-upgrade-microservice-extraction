package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
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

@WebMvcTest({StatsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class StatsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatsReadService articleStatsReadService;

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
            new TrendingArticleData("id1", "popular-article", "Popular Article", "Desc 1", 15),
            new TrendingArticleData("id2", "another-popular", "Another Popular", "Desc 2", 10));

    when(articleStatsReadService.findTrendingArticles(eq(7), eq(10))).thenReturn(trending);

    given()
        .when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(2))
        .body("articles[0].slug", equalTo("popular-article"))
        .body("articles[0].favoriteCount", equalTo(15))
        .body("articles[1].slug", equalTo("another-popular"))
        .body("articles[1].favoriteCount", equalTo(10));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(articleStatsReadService.findTrendingArticles(eq(7), eq(10))).thenReturn(new ArrayList<>());

    given()
        .when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(0))
        .body("articles.size()", equalTo(0));
  }

  @Test
  public void should_get_trending_without_authentication() throws Exception {
    List<TrendingArticleData> trending =
        Arrays.asList(new TrendingArticleData("id1", "top-article", "Top Article", "Desc", 20));

    when(articleStatsReadService.findTrendingArticles(eq(7), eq(10))).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(1))
        .body("articles[0].title", equalTo("Top Article"));
  }
}
