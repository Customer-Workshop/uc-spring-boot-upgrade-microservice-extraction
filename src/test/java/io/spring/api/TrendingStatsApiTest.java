package io.spring.api;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
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

@WebMvcTest({TrendingStatsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class TrendingStatsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatsQueryService articleStatsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    List<TrendingArticleData> trendingList =
        Arrays.asList(
            new TrendingArticleData("article-one", "Article One", "Desc 1", "user1", 50),
            new TrendingArticleData("article-two", "Article Two", "Desc 2", "user2", 30),
            new TrendingArticleData("article-three", "Article Three", "Desc 3", "user3", 10));

    when(articleStatsQueryService.getTrendingArticles(eq(7), eq(10))).thenReturn(trendingList);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles[0].slug", equalTo("article-one"))
        .body("articles[0].favoriteCount", equalTo(50))
        .body("articles[1].slug", equalTo("article-two"))
        .body("articles[2].slug", equalTo("article-three"))
        .body("articlesCount", equalTo(3));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(articleStatsQueryService.getTrendingArticles(eq(7), eq(10)))
        .thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(0));
  }
}
