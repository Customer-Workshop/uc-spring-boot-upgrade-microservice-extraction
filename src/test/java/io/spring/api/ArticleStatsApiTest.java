package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.Arrays;
import java.util.Collections;
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

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    DateTime createdAt = new DateTime().minusDays(5);
    Article article =
        new Article("Test Article", "desc", "body", Arrays.asList("java"), user.getId(), createdAt);

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleStatsReadService.getViewCount(eq(article.getId()))).thenReturn(42);
    when(articleStatsReadService.getFavoriteCount(eq(article.getId()))).thenReturn(3);
    when(articleStatsReadService.getCommentCount(eq(article.getId()))).thenReturn(7);

    given()
        .when()
        .get("/articles/{slug}/stats", article.getSlug())
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(42))
        .body("stats.favoriteCount", equalTo(3))
        .body("stats.commentCount", equalTo(7))
        .body("stats.daysSincePublished", equalTo(5));
  }

  @Test
  public void should_return_404_for_nonexistent_article_stats() throws Exception {
    when(articleRepository.findBySlug(eq("nonexistent-slug"))).thenReturn(Optional.empty());

    given().when().get("/articles/{slug}/stats", "nonexistent-slug").then().statusCode(404);
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    TrendingArticleData trending1 =
        new TrendingArticleData("spring-boot-guide", "Spring Boot Guide", "A guide", 10);
    TrendingArticleData trending2 =
        new TrendingArticleData("docker-tutorial", "Docker Tutorial", "Docker basics", 7);

    when(articleStatsReadService.findTrendingArticles(eq(7), eq(10)))
        .thenReturn(Arrays.asList(trending1, trending2));

    given()
        .when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles.size()", equalTo(2))
        .body("articlesCount", equalTo(2))
        .body("articles[0].slug", equalTo("spring-boot-guide"))
        .body("articles[0].favoriteCount", equalTo(10))
        .body("articles[1].slug", equalTo("docker-tutorial"))
        .body("articles[1].favoriteCount", equalTo(7));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(articleStatsReadService.findTrendingArticles(eq(7), eq(10)))
        .thenReturn(Collections.emptyList());

    given()
        .when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles.size()", equalTo(0))
        .body("articlesCount", equalTo(0));
  }

  @Test
  public void should_get_stats_for_article_with_zero_counts() throws Exception {
    DateTime createdAt = new DateTime();
    Article article =
        new Article(
            "Brand New Article", "desc", "body", Arrays.asList("test"), user.getId(), createdAt);

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleStatsReadService.getViewCount(eq(article.getId()))).thenReturn(0);
    when(articleStatsReadService.getFavoriteCount(eq(article.getId()))).thenReturn(0);
    when(articleStatsReadService.getCommentCount(eq(article.getId()))).thenReturn(0);

    given()
        .when()
        .get("/articles/{slug}/stats", article.getSlug())
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(0))
        .body("stats.commentCount", equalTo(0))
        .body("stats.daysSincePublished", equalTo(0));
  }
}
