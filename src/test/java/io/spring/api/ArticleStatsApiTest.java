package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
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

  @MockBean private ArticleRepository articleRepository;

  @MockBean private ArticleStatsReadService articleStatsReadService;

  private Article article;
  private String slug;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);

    slug = "test-article";
    article =
        new Article(
            "Test Article",
            "Description",
            "Body",
            Arrays.asList("java"),
            user.getId(),
            new DateTime().minusDays(5));

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    when(articleStatsReadService.countViewsByArticleId(eq(article.getId()))).thenReturn(10);
    when(articleStatsReadService.countFavoritesByArticleId(eq(article.getId()))).thenReturn(3);
    when(articleStatsReadService.countCommentsByArticleId(eq(article.getId()))).thenReturn(7);

    given()
        .when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(10))
        .body("stats.favoriteCount", equalTo(3))
        .body("stats.commentCount", equalTo(7))
        .body("stats.daysSincePublished", equalTo(5));
  }

  @Test
  public void should_get_article_stats_with_zero_counts() throws Exception {
    when(articleStatsReadService.countViewsByArticleId(eq(article.getId()))).thenReturn(0);
    when(articleStatsReadService.countFavoritesByArticleId(eq(article.getId()))).thenReturn(0);
    when(articleStatsReadService.countCommentsByArticleId(eq(article.getId()))).thenReturn(0);

    given()
        .when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(0))
        .body("stats.commentCount", equalTo(0));
  }

  @Test
  public void should_return_404_if_article_not_found() throws Exception {
    when(articleRepository.findBySlug(eq("non-existent"))).thenReturn(Optional.empty());

    given().when().get("/articles/{slug}/stats", "non-existent").then().statusCode(404);
  }

  @Test
  public void should_get_stats_without_authentication() throws Exception {
    when(articleStatsReadService.countViewsByArticleId(eq(article.getId()))).thenReturn(5);
    when(articleStatsReadService.countFavoritesByArticleId(eq(article.getId()))).thenReturn(2);
    when(articleStatsReadService.countCommentsByArticleId(eq(article.getId()))).thenReturn(1);

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(5))
        .body("stats.favoriteCount", equalTo(2))
        .body("stats.commentCount", equalTo(1));
  }
}
