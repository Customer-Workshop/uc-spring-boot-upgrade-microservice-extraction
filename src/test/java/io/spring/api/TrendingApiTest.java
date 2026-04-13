package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ProfileData;
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

@WebMvcTest(TrendingApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class TrendingApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatsQueryService articleStatsQueryService;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    ProfileData profileData = new ProfileData("user-1", "johndoe", "bio", "image", false);
    TrendingArticleData trending1 =
        new TrendingArticleData(
            "article-slug-1", "Article Title 1", "Description 1", 10, profileData);
    TrendingArticleData trending2 =
        new TrendingArticleData(
            "article-slug-2", "Article Title 2", "Description 2", 5, profileData);
    List<TrendingArticleData> trendingList = Arrays.asList(trending1, trending2);

    when(articleStatsQueryService.getTrendingArticles(eq(7), eq(10))).thenReturn(trendingList);

    given()
        .when()
        .get("/stats/trending")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("articles[0].slug", equalTo("article-slug-1"))
        .body("articles[0].title", equalTo("Article Title 1"))
        .body("articles[0].favoriteCount", equalTo(10))
        .body("articles[1].slug", equalTo("article-slug-2"))
        .body("articles[1].favoriteCount", equalTo(5))
        .body("articlesCount", equalTo(2));
  }

  @Test
  public void should_return_empty_list_when_no_trending() throws Exception {
    when(articleStatsQueryService.getTrendingArticles(eq(7), eq(10)))
        .thenReturn(Collections.emptyList());

    given().when().get("/stats/trending").then().statusCode(200).body("articlesCount", equalTo(0));
  }

  @Test
  public void should_get_trending_without_authentication() throws Exception {
    when(articleStatsQueryService.getTrendingArticles(eq(7), eq(10)))
        .thenReturn(Collections.emptyList());

    given().when().get("/stats/trending").then().statusCode(200);
  }
}
