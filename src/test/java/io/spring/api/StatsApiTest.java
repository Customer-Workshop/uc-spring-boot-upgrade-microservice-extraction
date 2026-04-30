package io.spring.api;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.TestHelper;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.infrastructure.mybatis.readservice.StatsReadService;
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

@WebMvcTest(StatsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class StatsApiTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @MockBean private StatsReadService statsReadService;

  @MockBean private ArticleQueryService articleQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    ArticleData article1 = TestHelper.articleDataFixture("1", user);
    ArticleData article2 = TestHelper.articleDataFixture("2", user);

    List<String> articleIds = Arrays.asList(article1.getId(), article2.getId());
    when(statsReadService.findTrendingArticleIds(eq(10))).thenReturn(articleIds);
    when(articleQueryService.findArticlesByIds(eq(articleIds), any()))
        .thenReturn(Arrays.asList(article1, article2));

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles.size()", equalTo(2))
        .body("articlesCount", equalTo(2))
        .body("articles[0].slug", equalTo(article1.getSlug()));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(statsReadService.findTrendingArticleIds(eq(10))).thenReturn(new ArrayList<>());
    when(articleQueryService.findArticlesByIds(eq(new ArrayList<>()), any()))
        .thenReturn(new ArrayList<>());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles.size()", equalTo(0))
        .body("articlesCount", equalTo(0));
  }
}
