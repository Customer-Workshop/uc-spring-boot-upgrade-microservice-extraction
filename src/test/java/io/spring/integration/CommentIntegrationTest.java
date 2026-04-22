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

public class CommentIntegrationTest extends BaseIntegrationTest {

  private String token;
  private String slug;

  @BeforeEach
  public void setUp() {
    String email = "comment" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    String username = "commentuser" + UUID.randomUUID().toString().substring(0, 8);
    token = registerAndGetToken(email, username, "password123");

    Map<String, Object> article = new HashMap<>();
    article.put("title", "Comment Test " + UUID.randomUUID().toString().substring(0, 8));
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
  public void testAddComment() {
    Map<String, Object> comment = new HashMap<>();
    comment.put("body", "This is a test comment");

    Map<String, Object> body = new HashMap<>();
    body.put("comment", comment);

    given()
        .header("Authorization", "Token " + token)
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("/articles/" + slug + "/comments")
        .then()
        .statusCode(201)
        .body("comment.body", equalTo("This is a test comment"));
  }

  @Test
  public void testGetComments() {
    Map<String, Object> comment = new HashMap<>();
    comment.put("body", "A comment to list");

    Map<String, Object> body = new HashMap<>();
    body.put("comment", comment);

    given()
        .header("Authorization", "Token " + token)
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("/articles/" + slug + "/comments");

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/" + slug + "/comments")
        .then()
        .statusCode(200)
        .body("comments", notNullValue())
        .body("comments.size()", greaterThanOrEqualTo(1));
  }

  @Test
  public void testDeleteComment() {
    Map<String, Object> comment = new HashMap<>();
    comment.put("body", "Comment to delete");

    Map<String, Object> body = new HashMap<>();
    body.put("comment", comment);

    String commentId =
        given()
            .header("Authorization", "Token " + token)
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post("/articles/" + slug + "/comments")
            .then()
            .extract()
            .jsonPath()
            .getString("comment.id");

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/" + slug + "/comments/" + commentId)
        .then()
        .statusCode(204);
  }
}
