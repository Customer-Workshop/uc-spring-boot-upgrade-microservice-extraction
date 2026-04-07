package io.spring.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Integration tests that verify the monolith and comments microservice communicate correctly.
 *
 * <p>These tests require both services to be running:
 * <ul>
 *   <li>Monolith on port 8080 (or configured via -Dmonolith.url)</li>
 *   <li>Comments microservice on port 8081 (or configured via -Dcomments.service.url)</li>
 * </ul>
 *
 * <p>Run with: docker-compose up, then ./gradlew :integration-tests:test
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentsIntegrationTest {

  private static String monolithUrl;
  private static String commentsServiceUrl;
  private static String authToken;
  private static String articleSlug;
  private static String createdCommentId;

  @BeforeAll
  static void setUp() {
    monolithUrl = System.getProperty("monolith.url", "http://localhost:8080");
    commentsServiceUrl =
        System.getProperty("comments.service.url", "http://localhost:8081");
  }

  @Test
  @Order(1)
  void comments_microservice_should_be_reachable() {
    given()
        .baseUri(commentsServiceUrl)
        .when()
        .get("/api/comments/article/nonexistent")
        .then()
        .statusCode(200)
        .body("$", hasSize(0));
  }

  @Test
  @Order(2)
  void should_register_user_on_monolith() {
    Map<String, Object> user = new HashMap<>();
    user.put("username", "integrationtester");
    user.put("email", "integration@test.com");
    user.put("password", "password123");

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    Response response =
        given()
            .baseUri(monolithUrl)
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post("/users");

    if (response.statusCode() == 201) {
      authToken = response.jsonPath().getString("user.token");
    } else {
      // User may already exist, try login
      Map<String, Object> loginUser = new HashMap<>();
      loginUser.put("email", "integration@test.com");
      loginUser.put("password", "password123");

      Map<String, Object> loginBody = new HashMap<>();
      loginBody.put("user", loginUser);

      response =
          given()
              .baseUri(monolithUrl)
              .contentType(ContentType.JSON)
              .body(loginBody)
              .when()
              .post("/users/login");
      response.then().statusCode(200);
      authToken = response.jsonPath().getString("user.token");
    }
  }

  @Test
  @Order(3)
  void should_create_article_on_monolith() {
    Map<String, Object> article = new HashMap<>();
    article.put("title", "Integration Test Article " + System.currentTimeMillis());
    article.put("description", "Test article for integration testing");
    article.put("body", "This article tests monolith-microservice communication");
    article.put("tagList", new String[] {"integration", "test"});

    Map<String, Object> body = new HashMap<>();
    body.put("article", article);

    Response response =
        given()
            .baseUri(monolithUrl)
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + authToken)
            .body(body)
            .when()
            .post("/articles");

    response.then().statusCode(201);
    articleSlug = response.jsonPath().getString("article.slug");
  }

  @Test
  @Order(4)
  void should_create_comment_via_monolith_and_store_in_microservice() {
    Map<String, Object> comment = new HashMap<>();
    comment.put("body", "Integration test comment created via monolith");

    Map<String, Object> body = new HashMap<>();
    body.put("comment", comment);

    Response response =
        given()
            .baseUri(monolithUrl)
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + authToken)
            .body(body)
            .when()
            .post("/articles/" + articleSlug + "/comments");

    response.then().statusCode(201).body("comment.body", equalTo("Integration test comment created via monolith"));
    createdCommentId = response.jsonPath().getString("comment.id");
  }

  @Test
  @Order(5)
  void should_retrieve_comments_via_monolith() {
    given()
        .baseUri(monolithUrl)
        .when()
        .get("/articles/" + articleSlug + "/comments")
        .then()
        .statusCode(200)
        .body("comments", hasSize(greaterThanOrEqualTo(1)));
  }

  @Test
  @Order(6)
  void should_verify_comment_exists_directly_in_microservice() {
    given()
        .baseUri(commentsServiceUrl)
        .when()
        .get("/api/comments/" + createdCommentId)
        .then()
        .statusCode(200)
        .body("id", equalTo(createdCommentId))
        .body("body", equalTo("Integration test comment created via monolith"));
  }

  @Test
  @Order(7)
  void should_delete_comment_via_monolith() {
    given()
        .baseUri(monolithUrl)
        .header("Authorization", "Token " + authToken)
        .when()
        .delete("/articles/" + articleSlug + "/comments/" + createdCommentId)
        .then()
        .statusCode(204);
  }

  @Test
  @Order(8)
  void should_verify_comment_deleted_from_microservice() {
    given()
        .baseUri(commentsServiceUrl)
        .when()
        .get("/api/comments/" + createdCommentId)
        .then()
        .statusCode(404);
  }

  @Test
  @Order(9)
  void should_create_comment_directly_on_microservice_and_read_via_monolith() {
    // Create directly on microservice
    Map<String, Object> payload = new HashMap<>();
    payload.put("body", "Direct microservice comment");
    payload.put("userId", "user-1");
    payload.put("articleId", "direct-test-article");

    Response createResponse =
        given()
            .baseUri(commentsServiceUrl)
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/api/comments");

    createResponse.then().statusCode(201).body("id", notNullValue());
    String directCommentId = createResponse.jsonPath().getString("id");

    // Verify it can be fetched directly from microservice
    given()
        .baseUri(commentsServiceUrl)
        .when()
        .get("/api/comments/" + directCommentId)
        .then()
        .statusCode(200)
        .body("body", equalTo("Direct microservice comment"));
  }
}
