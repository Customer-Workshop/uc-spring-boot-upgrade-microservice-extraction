package io.spring.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.TrendingArticleData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({TrendingArticlesApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class TrendingArticlesApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatisticsQueryService articleStatisticsQueryService;

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
                "id-1", "popular-article", "Popular Article", "A popular one", 10),
            new TrendingArticleData(
                "id-2", "another-hit", "Another Hit", "Another popular one", 7));

    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(2))
        .body("articlesCount", equalTo(2))
        .body("articles[0].slug", equalTo("popular-article"))
        .body("articles[0].favoriteCount", equalTo(10))
        .body("articles[1].slug", equalTo("another-hit"))
        .body("articles[1].favoriteCount", equalTo(7));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0))
        .body("articlesCount", equalTo(0));
  }

  @Test
  public void should_not_require_authentication() throws Exception {
    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when().get("/stats/trending").then().statusCode(200);
  }
}
