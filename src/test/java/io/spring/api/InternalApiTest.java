package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.internal.InternalApi;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
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

@WebMvcTest(controllers = InternalApi.class, properties = "internal.api.enabled=true")
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class InternalApiTest extends TestWithCurrentUser {

  @MockBean private ArticleQueryService articleQueryService;
  @MockBean private UserRelationshipQueryService userRelationshipQueryService;

  @Autowired private MockMvc mvc;

  private Article article;
  private ArticleData articleData;

  @BeforeEach
  public void setUp() throws Exception {
    RestAssuredMockMvc.mockMvc(mvc);
    super.setUp();
    article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
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
            Arrays.asList("java"),
            new ProfileData(
                user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
  }

  @Test
  public void should_return_article_with_author_id_for_slug() {
    when(articleQueryService.findBySlug(eq(article.getSlug()), eq(null)))
        .thenReturn(Optional.of(articleData));

    given()
        .when()
        .get("/internal/articles/{slug}", article.getSlug())
        .then()
        .statusCode(200)
        .body("article.id", equalTo(article.getId()))
        .body("article.slug", equalTo(article.getSlug()))
        .body("article.tagList[0]", equalTo("java"))
        .body("article.author.id", equalTo(user.getId()))
        .body("article.author.username", equalTo(user.getUsername()))
        .body("article.author.following", equalTo(false));
  }

  @Test
  public void should_return_404_when_slug_is_unknown() {
    when(articleQueryService.findBySlug(eq("nope"), eq(null))).thenReturn(Optional.empty());

    given().when().get("/internal/articles/{slug}", "nope").then().statusCode(404);
  }

  @Test
  public void should_return_profile_by_id_with_following_flag() {
    when(userRelationshipQueryService.isUserFollowing(eq("viewer"), eq(user.getId())))
        .thenReturn(true);

    given()
        .queryParam("viewerId", "viewer")
        .when()
        .get("/internal/users/{id}", user.getId())
        .then()
        .statusCode(200)
        .body("profile.id", equalTo(user.getId()))
        .body("profile.username", equalTo(user.getUsername()))
        .body("profile.following", equalTo(true));
  }

  @Test
  public void should_return_404_for_unknown_profile() {
    given().when().get("/internal/users/{id}", "missing").then().statusCode(404);
  }

  @Test
  public void should_return_profiles_in_batch() {
    when(userRelationshipQueryService.followingAuthors(
            eq("viewer"), eq(Collections.singletonList(user.getId()))))
        .thenReturn(Collections.singleton(user.getId()));

    given()
        .queryParam("ids", user.getId())
        .queryParam("viewerId", "viewer")
        .when()
        .get("/internal/users")
        .then()
        .statusCode(200)
        .body("profiles[0].id", equalTo(user.getId()))
        .body("profiles[0].following", equalTo(true));
  }
}
