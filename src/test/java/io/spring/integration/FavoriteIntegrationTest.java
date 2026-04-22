package io.spring.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.http.ContentType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FavoriteIntegrationTest extends BaseIntegrationTest {

  private String token;
  private String slug;

  @BeforeEach
  public void setUp() {
    String email = "fav" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    String username = "favuser" + UUID.randomUUID().toString().substring(0, 8);
    token = registerAndGetToken(email, username, "password123");

    Map<String, Object> article = new HashMap<>();
    article.put("title", "Favorite Test " + UUID.randomUUID().toString().substring(0, 8));
    article.put("description", "Test description");
    article.put("body", "Test body");
    article.put("tagList", Arrays.asList("test"));

    Map<String, Object> body = new HashMap<>();
    body.put("article", article);

    slug =
        given()
            .header("Authorization", "Token " + token)
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post("/articles")
            .then()
            .extract()
            .jsonPath()
            .getString("article.slug");
  }

  @Test
  public void testFavoriteArticle() {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/" + slug + "/favorite")
        .then()
        .statusCode(200)
        .body("article.favorited", is(true))
        .body("article.favoritesCount", equalTo(1));
  }

  @Test
  public void testUnfavoriteArticle() {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .post("/articles/" + slug + "/favorite");

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/" + slug + "/favorite")
        .then()
        .statusCode(200)
        .body("article.favorited", is(false))
        .body("article.favoritesCount", equalTo(0));
  }
}
