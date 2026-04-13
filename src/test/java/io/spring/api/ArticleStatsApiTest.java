package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.infrastructure.mybatis.readservice.StatsReadService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticleStatsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleStatsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private StatsReadService statsReadService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    String slug = "test-article";
    Map<String, Object> rawStats = new HashMap<>();
    rawStats.put("slug", slug);
    rawStats.put("title", "Test Article");
    rawStats.put("viewCount", 150);
    rawStats.put("favoriteCount", 5);
    rawStats.put("commentCount", 3);
    rawStats.put("daysSincePublished", 7L);

    when(statsReadService.getArticleStatsBySlug(eq(slug))).thenReturn(rawStats);

    given()
        .when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.title", equalTo("Test Article"))
        .body("stats.viewCount", equalTo(150))
        .body("stats.favoriteCount", equalTo(5))
        .body("stats.commentCount", equalTo(3))
        .body("stats.daysSincePublished", equalTo(7));
  }

  @Test
  public void should_return_404_when_article_not_found() throws Exception {
    when(statsReadService.getArticleStatsBySlug(eq("nonexistent-article"))).thenReturn(null);

    given().when().get("/articles/{slug}/stats", "nonexistent-article").then().statusCode(404);
  }

  @Test
  public void should_return_zero_counts_for_new_article() throws Exception {
    String slug = "brand-new-article";
    Map<String, Object> rawStats = new HashMap<>();
    rawStats.put("slug", slug);
    rawStats.put("title", "Brand New Article");
    rawStats.put("viewCount", 0);
    rawStats.put("favoriteCount", 0);
    rawStats.put("commentCount", 0);
    rawStats.put("daysSincePublished", 0L);

    when(statsReadService.getArticleStatsBySlug(eq(slug))).thenReturn(rawStats);

    given()
        .when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(0))
        .body("stats.commentCount", equalTo(0))
        .body("stats.daysSincePublished", equalTo(0));
  }
}
