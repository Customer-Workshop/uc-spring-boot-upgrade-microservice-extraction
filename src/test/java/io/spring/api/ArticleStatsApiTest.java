package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

  @MockBean private ArticleStatsQueryService articleStatsQueryService;

  @MockBean private ArticleRepository articleRepository;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_stats_for_existing_article() throws Exception {
    String slug = "test-article";
    DateTime createdAt = new DateTime().minusDays(7);
    Article article =
        new Article("Test Article", "Desc", "Body", Arrays.asList("java"), user.getId(), createdAt);

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

    Map<String, Object> stats = new HashMap<>();
    stats.put("favoriteCount", 12);
    stats.put("commentCount", 5);
    stats.put("daysSincePublished", 7);
    Map<String, Object> response = new HashMap<>();
    response.put("stats", stats);

    when(articleStatsQueryService.getArticleStats(eq(article.getId()), eq(createdAt)))
        .thenReturn(response);

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.favoriteCount", equalTo(12))
        .body("stats.commentCount", equalTo(5))
        .body("stats.daysSincePublished", equalTo(7));
  }

  @Test
  public void should_return_404_for_nonexistent_slug() throws Exception {
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", "non-existent-slug")
        .then()
        .statusCode(404);
  }
}
