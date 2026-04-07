package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.spring.TestHelper.articleDataFixture;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.ArticleRepository;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticlesApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class TrendingArticlesApiTest extends TestWithCurrentUser {
  @MockBean private ArticleRepository articleRepository;

  @MockBean private ArticleQueryService articleQueryService;

  @MockBean private ArticleCommandService articleCommandService;

  @Autowired private MockMvc mvc;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_trending_articles() throws Exception {
    ArticleDataList articleDataList =
        new ArticleDataList(
            asList(articleDataFixture("1", user), articleDataFixture("2", user)), 2);
    when(articleQueryService.findTrendingArticles(eq(10), eq(7), eq(null)))
        .thenReturn(articleDataList);

    RestAssuredMockMvc.when()
        .get("/articles/trending")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(2));
  }

  @Test
  public void should_return_empty_list_when_no_recent_articles() throws Exception {
    ArticleDataList emptyList = new ArticleDataList(new ArrayList<>(), 0);
    when(articleQueryService.findTrendingArticles(eq(10), eq(7), eq(null)))
        .thenReturn(emptyList);

    RestAssuredMockMvc.when()
        .get("/articles/trending")
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("articlesCount", equalTo(0))
        .body("articles.size()", equalTo(0));
  }
}
