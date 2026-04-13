package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ArticleStatsData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleStatsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleStatsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleRepository articleRepository;

  @MockBean private ArticleStatsQueryService articleStatsQueryService;

  private Article article;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
    article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    ArticleStatsData statsData =
        new ArticleStatsData(article.getSlug(), article.getTitle(), 10, 5, 3, 7);
    when(articleStatsQueryService.getArticleStats(eq(article))).thenReturn(statsData);

    given()
        .when()
        .get("/articles/{slug}/stats", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(article.getSlug()))
        .body("stats.title", equalTo(article.getTitle()))
        .body("stats.viewCount", equalTo(10))
        .body("stats.favoriteCount", equalTo(5))
        .body("stats.commentCount", equalTo(3))
        .body("stats.daysSincePublished", equalTo(7));
  }

  @Test
  public void should_get_404_if_article_not_found() throws Exception {
    when(articleRepository.findBySlug(eq("nonexistent-slug"))).thenReturn(Optional.empty());

    given().when().get("/articles/{slug}/stats", "nonexistent-slug").then().statusCode(404);
  }

  @Test
  public void should_get_stats_without_authentication() throws Exception {
    ArticleStatsData statsData =
        new ArticleStatsData(article.getSlug(), article.getTitle(), 0, 0, 0, 0);
    when(articleStatsQueryService.getArticleStats(eq(article))).thenReturn(statsData);

    given()
        .when()
        .get("/articles/{slug}/stats", article.getSlug())
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(0))
        .body("stats.commentCount", equalTo(0));
  }
}
