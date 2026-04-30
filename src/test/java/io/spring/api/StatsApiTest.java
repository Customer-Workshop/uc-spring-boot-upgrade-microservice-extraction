package io.spring.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsService;
import io.spring.application.data.TrendingArticleData;
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

  @MockBean private ArticleStatsService articleStatsService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_trending_articles() throws Exception {
    List<TrendingArticleData> trending =
        Arrays.asList(
            new TrendingArticleData(
                "popular-article", "Popular Article", "A popular one", "johndoe", 15),
            new TrendingArticleData(
                "another-popular", "Another Popular", "Also popular", "janedoe", 10));

    when(articleStatsService.getTrendingArticles()).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(2))
        .body("articles[0].slug", equalTo("popular-article"))
        .body("articles[0].title", equalTo("Popular Article"))
        .body("articles[0].favoriteCount", equalTo(15))
        .body("articles[0].author", equalTo("johndoe"))
        .body("articles[1].slug", equalTo("another-popular"))
        .body("articles[1].favoriteCount", equalTo(10));
  }

  @Test
  public void should_return_empty_list_when_no_trending() throws Exception {
    when(articleStatsService.getTrendingArticles()).thenReturn(new ArrayList<>());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0));
  }

  @Test
  public void should_get_trending_without_auth() throws Exception {
    List<TrendingArticleData> trending =
        Arrays.asList(
            new TrendingArticleData("top-article", "Top Article", "The best", "author1", 20));

    when(articleStatsService.getTrendingArticles()).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(1))
        .body("articles[0].slug", equalTo("top-article"));
  }
}
