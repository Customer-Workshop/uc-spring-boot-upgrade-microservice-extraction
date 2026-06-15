package io.spring.articleservice.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.articleservice.JacksonCustomizations;
import io.spring.articleservice.api.security.WebSecurityConfig;
import io.spring.articleservice.application.ArticleQueryService;
import io.spring.articleservice.application.article.ArticleCommandService;
import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.application.data.ProfileData;
import io.spring.articleservice.core.article.Article;
import io.spring.articleservice.core.article.ArticleRepository;
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

@WebMvcTest({ArticleApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleQueryService articleQueryService;

  @MockBean private ArticleRepository articleRepository;

  @MockBean private ArticleCommandService articleCommandService;

  private Article article;
  private ArticleData articleData;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);

    article = new Article("test title", "test desc", "test body", asList("java"), userId);
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
            asList("java"),
            new ProfileData(userId, username, "", defaultAvatar, false));

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleQueryService.findBySlug(eq(article.getSlug()), any()))
        .thenReturn(Optional.of(articleData));
  }

  @Test
  public void should_get_article_success() {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/{slug}", article.getSlug())
        .then()
        .statusCode(200)
        .body("article.slug", equalTo(article.getSlug()));
  }

  @Test
  public void should_get_article_without_authorization() {
    given()
        .when()
        .get("/articles/{slug}", article.getSlug())
        .then()
        .statusCode(200)
        .body("article.slug", equalTo(article.getSlug()));
  }

  @Test
  public void should_404_if_article_not_found() {
    when(articleQueryService.findBySlug(eq("not-exists"), any())).thenReturn(Optional.empty());

    given().when().get("/articles/{slug}", "not-exists").then().statusCode(404);
  }

  @Test
  public void should_update_article_success() {
    String newTitle = "new title";
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "article",
                new HashMap<String, Object>() {
                  {
                    put("title", newTitle);
                    put("body", "");
                    put("description", "");
                  }
                });
          }
        };

    Article updatedArticle =
        new Article(newTitle, article.getDescription(), article.getBody(), asList("java"), userId);
    ArticleData updatedArticleData =
        new ArticleData(
            updatedArticle.getId(),
            updatedArticle.getSlug(),
            newTitle,
            article.getDescription(),
            article.getBody(),
            false,
            0,
            new DateTime(),
            new DateTime(),
            asList("java"),
            new ProfileData(userId, username, "", defaultAvatar, false));

    when(articleCommandService.updateArticle(any(), any())).thenReturn(updatedArticle);
    when(articleQueryService.findBySlug(eq(updatedArticle.getSlug()), any()))
        .thenReturn(Optional.of(updatedArticleData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/articles/{slug}", article.getSlug())
        .then()
        .statusCode(200)
        .body("article.title", equalTo(newTitle));
  }

  @Test
  public void should_403_if_not_author() {
    String anotherUserId = "another-user-id";
    Article anotherArticle =
        new Article("another title", "desc", "body", asList("java"), anotherUserId);
    when(articleRepository.findBySlug(eq(anotherArticle.getSlug())))
        .thenReturn(Optional.of(anotherArticle));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(
            new HashMap<String, Object>() {
              {
                put(
                    "article",
                    new HashMap<String, Object>() {
                      {
                        put("title", "new");
                        put("body", "");
                        put("description", "");
                      }
                    });
              }
            })
        .when()
        .put("/articles/{slug}", anotherArticle.getSlug())
        .then()
        .statusCode(403);
  }

  @Test
  public void should_delete_article_success() {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}", article.getSlug())
        .then()
        .statusCode(204);
  }
}
