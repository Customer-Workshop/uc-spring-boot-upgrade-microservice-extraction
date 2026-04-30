package io.spring.api;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.StatsReadService;
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

@WebMvcTest(ArticleStatsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleStatsApiTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @MockBean private ArticleRepository articleRepository;

  @MockBean private StatsReadService statsReadService;

  private Article article;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
    article =
        new Article(
            "Test Article",
            "desc",
            "body",
            Arrays.asList("java"),
            user.getId(),
            new DateTime().minusDays(5));
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    when(statsReadService.getViewCount(eq(article.getId()))).thenReturn(10);
    when(statsReadService.getFavoriteCount(eq(article.getId()))).thenReturn(3);
    when(statsReadService.getCommentCount(eq(article.getId()))).thenReturn(7);

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", article.getSlug())
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(10))
        .body("stats.favoriteCount", equalTo(3))
        .body("stats.commentCount", equalTo(7))
        .body("stats.daysSincePublished", equalTo(5));
  }

  @Test
  public void should_get_stats_with_zero_counts() throws Exception {
    when(statsReadService.getViewCount(eq(article.getId()))).thenReturn(0);
    when(statsReadService.getFavoriteCount(eq(article.getId()))).thenReturn(0);
    when(statsReadService.getCommentCount(eq(article.getId()))).thenReturn(0);

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", article.getSlug())
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(0))
        .body("stats.commentCount", equalTo(0));
  }

  @Test
  public void should_return_404_for_nonexistent_article() throws Exception {
    when(articleRepository.findBySlug(eq("nonexistent-slug"))).thenReturn(Optional.empty());

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", "nonexistent-slug")
        .then()
        .statusCode(404);
  }
}
