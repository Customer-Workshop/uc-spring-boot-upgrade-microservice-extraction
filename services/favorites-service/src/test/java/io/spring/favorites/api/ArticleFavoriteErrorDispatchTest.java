package io.spring.favorites.api;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.RestAssured;
import io.spring.favorites.api.exception.MonolithUnavailableException;
import io.spring.favorites.api.exception.ResourceNotFoundException;
import io.spring.favorites.core.favorite.ArticleFavoriteRepository;
import io.spring.favorites.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Runs against a real servlet container so the ERROR dispatch is exercised; MockMvc skips it and
 * therefore cannot catch the security filter chain swallowing {@code sendError} statuses.
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArticleFavoriteErrorDispatchTest extends TestWithCurrentUser {

  @LocalServerPort private int port;

  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;

  @MockBean private ArticleFavoritesReadService articleFavoritesReadService;

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssured.port = port;
    when(monolithClient.findArticleBySlug(eq("no-such-article"), eq(user.getId())))
        .thenThrow(new ResourceNotFoundException());
    when(monolithClient.findArticleBySlug(eq("flaky"), eq(user.getId())))
        .thenThrow(
            new MonolithUnavailableException("GET /internal/articles/flaky", "read timed out"));
  }

  @Test
  public void should_404_on_post_for_an_unknown_slug() {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/favorite", "no-such-article")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_404_on_delete_for_an_unknown_slug() {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/favorite", "no-such-article")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_503_when_the_monolith_is_unavailable() {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/{slug}/favorite", "flaky")
        .then()
        .statusCode(503);
  }

  @Test
  public void should_401_without_a_token() {
    given().when().post("/articles/{slug}/favorite", "no-such-article").then().statusCode(401);
  }
}
