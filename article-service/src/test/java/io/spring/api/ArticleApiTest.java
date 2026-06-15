package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.JacksonCustomizations;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.JwtService;
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

@WebMvcTest(ArticleApi.class)
@Import(JacksonCustomizations.class)
class ArticleApiTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ArticleQueryService articleQueryService;
  @MockBean private ArticleRepository articleRepository;
  @MockBean private ArticleCommandService articleCommandService;
  @MockBean private JwtService jwtService;

  private ArticleData articleData;
  private Article article;
  private String userId = "user-1";

  @BeforeEach
  void setUp() {
    article = new Article("Test Article", "desc", "body", Arrays.asList("java", "spring"), userId);

    ProfileData profileData = new ProfileData(userId, "johndoe", "bio", "image", false);
    articleData =
        new ArticleData(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getDescription(),
            article.getBody(),
            false,
            0,
            new DateTime(),
            new DateTime(),
            Arrays.asList("java", "spring"),
            userId,
            profileData);
  }

  @Test
  void shouldGetArticleBySlug() {
    when(jwtService.getSubFromToken(any())).thenReturn(Optional.of(userId));
    when(articleQueryService.findBySlug(eq(article.getSlug()), any()))
        .thenReturn(Optional.of(articleData));

    given()
        .mockMvc(mockMvc)
        .header("Authorization", "Token testtoken")
        .when()
        .get("/articles/{slug}", article.getSlug())
        .then()
        .statusCode(200)
        .body("article.title", equalTo("Test Article"));
  }

  @Test
  void shouldReturn404ForNonExistentArticle() {
    when(articleQueryService.findBySlug(eq("non-existent"), any())).thenReturn(Optional.empty());

    given().mockMvc(mockMvc).when().get("/articles/{slug}", "non-existent").then().statusCode(404);
  }
}
