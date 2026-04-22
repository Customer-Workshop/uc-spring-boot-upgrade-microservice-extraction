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

public class ArticleIntegrationTest extends BaseIntegrationTest {

  private String token;
  private String username;

  @BeforeEach
  public void setUp() {
    String email = "article" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    username = "articleuser" + UUID.randomUUID().toString().substring(0, 8);
    token = registerAndGetToken(email, username, "password123");
  }

  private Map<String, Object> createArticleBody(
      String title, String description, String body, String... tags) {
    Map<String, Object> article = new HashMap<>();
    article.put("title", title);
    article.put("description", description);
    article.put("body", body);
    article.put("tagList", Arrays.asList(tags));

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("article", article);
    return requestBody;
  }

  private String createArticleAndGetSlug(String title) {
    Map<String, Object> body = createArticleBody(title, "Test description", "Test body", "test");

    return given()
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
  public void testCreateArticle() {
    String title = "Test Article " + UUID.randomUUID().toString().substring(0, 8);
    Map<String, Object> body =
        createArticleBody(title, "A test description", "The article body", "java", "testing");

    given()
        .header("Authorization", "Token " + token)
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("/articles")
        .then()
        .statusCode(200)
        .body("article.title", equalTo(title))
        .body("article.description", equalTo("A test description"))
        .body("article.body", equalTo("The article body"))
        .body("article.tagList", hasItems("java", "testing"))
        .body("article.slug", notNullValue());
  }

  @Test
  public void testGetArticle() {
    String title = "Get Article " + UUID.randomUUID().toString().substring(0, 8);
    String slug = createArticleAndGetSlug(title);

    given().when().get("/articles/" + slug).then().statusCode(200).body("article.slug", equalTo(slug))
        .body("article.title", equalTo(title));
  }

  @Test
  public void testUpdateArticle() {
    String slug =
        createArticleAndGetSlug(
            "Update Article " + UUID.randomUUID().toString().substring(0, 8));

    Map<String, Object> article = new HashMap<>();
    article.put("body", "Updated body content");

    Map<String, Object> body = new HashMap<>();
    body.put("article", article);

    given()
        .header("Authorization", "Token " + token)
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .put("/articles/" + slug)
        .then()
        .statusCode(200)
        .body("article.body", equalTo("Updated body content"));
  }

  @Test
  public void testDeleteArticle() {
    String slug =
        createArticleAndGetSlug(
            "Delete Article " + UUID.randomUUID().toString().substring(0, 8));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/" + slug)
        .then()
        .statusCode(204);
  }

  @Test
  public void testListArticles() {
    createArticleAndGetSlug("List Article " + UUID.randomUUID().toString().substring(0, 8));

    given()
        .when()
        .get("/articles")
        .then()
        .statusCode(200)
        .body("articles", notNullValue())
        .body("articlesCount", greaterThanOrEqualTo(1));
  }

  @Test
  public void testFeed() {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200)
        .body("articles", notNullValue())
        .body("articlesCount", notNullValue());
  }
}
