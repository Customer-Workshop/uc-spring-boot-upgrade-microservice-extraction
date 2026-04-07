package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.when;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.ArticleStatisticsData;
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
  public void should_get_article_statistics_success() throws Exception {
    String slug = "test-article";
    ArticleStatisticsData statsData = new ArticleStatisticsData(slug, 0, 5, 3, 7);

    when(articleStatisticsQueryService.getArticleStatistics(eq(slug)))
        .thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(5))
        .body("stats.commentCount", equalTo(3))
        .body("stats.daysSincePublished", equalTo(7));
  }

  @Test
  public void should_return_404_when_article_not_found() throws Exception {
    when(articleStatisticsQueryService.getArticleStatistics(eq("non-existent")))
        .thenReturn(Optional.empty());

    RestAssuredMockMvc.when().get("/articles/{slug}/stats", "non-existent").then().statusCode(404);
  }

  @Test
  public void should_get_statistics_with_zero_counts() throws Exception {
    String slug = "new-article";
    ArticleStatisticsData statsData = new ArticleStatisticsData(slug, 0, 0, 0, 0);

    when(articleStatisticsQueryService.getArticleStatistics(eq(slug)))
        .thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.favoriteCount", equalTo(0))
        .body("stats.commentCount", equalTo(0))
        .body("stats.daysSincePublished", equalTo(0));
  }
}
