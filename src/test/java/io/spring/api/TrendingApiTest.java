package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ProfileData;
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
  public void should_get_trending_articles_success() throws Exception {
    ProfileData author =
        new ProfileData("user-1", "johndoe", "Full-stack developer", "image-url", false);
    TrendingArticleData article1 =
        new TrendingArticleData("spring-boot-guide", "Spring Boot Guide", "A guide", 10, author);
    TrendingArticleData article2 =
        new TrendingArticleData("rest-api-tips", "REST API Tips", "Tips", 8, author);

    List<TrendingArticleData> trending = Arrays.asList(article1, article2);
    when(articleStatsQueryService.getTrendingArticles()).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(2))
        .body("articlesCount", equalTo(2))
        .body("articles[0].slug", equalTo("spring-boot-guide"))
        .body("articles[0].title", equalTo("Spring Boot Guide"))
        .body("articles[0].favoriteCount", equalTo(10))
        .body("articles[0].author.username", equalTo("johndoe"))
        .body("articles[1].slug", equalTo("rest-api-tips"))
        .body("articles[1].favoriteCount", equalTo(8));
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
  public void should_not_require_authentication_for_trending() throws Exception {
    when(articleStatsQueryService.getTrendingArticles()).thenReturn(new ArrayList<>());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200);
  }
}
