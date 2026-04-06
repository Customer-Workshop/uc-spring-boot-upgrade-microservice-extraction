package io.spring.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
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
 * Integration tests that verify the monolith and comments microservice communicate correctly. These
 * tests assume both services are running (e.g., via docker-compose).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentsIntegrationTest {

  private static String commentsServiceUrl;
  private static String monolithUrl;
  private static String createdCommentId;

  @BeforeAll
  static void setUp() {
    commentsServiceUrl = System.getProperty("comments.service.url", "http://localhost:8081");
    monolithUrl = System.getProperty("monolith.url", "http://localhost:8080");
  }

  @Test
  @Order(1)
  void commentsServiceHealthCheck() {
    given()
        .baseUri(commentsServiceUrl)
        .when()
        .get("/api/comments/article/test-health")
        .then()
        .statusCode(200);
  }

  @Test
  @Order(2)
  void shouldCreateCommentViaCommentsService() {
    Map<String, String> request = new HashMap<>();
    request.put("body", "Integration test comment");
    request.put("userId", "user-1");
    request.put("articleId", "article-1");

    Response response =
        given()
            .baseUri(commentsServiceUrl)
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/comments")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("body", equalTo("Integration test comment"))
            .body("userId", equalTo("user-1"))
            .body("articleId", equalTo("article-1"))
            .extract()
            .response();

    createdCommentId = response.jsonPath().getString("id");
  }

  @Test
  @Order(3)
  void shouldRetrieveCommentFromCommentsService() {
    given()
        .baseUri(commentsServiceUrl)
        .when()
        .get("/api/comments/" + createdCommentId)
        .then()
        .statusCode(200)
        .body("id", equalTo(createdCommentId))
        .body("body", equalTo("Integration test comment"));
  }

  @Test
  @Order(4)
  void shouldListCommentsByArticleFromCommentsService() {
    given()
        .baseUri(commentsServiceUrl)
        .when()
        .get("/api/comments/article/article-1")
        .then()
        .statusCode(200)
        .body("size()", greaterThan(0));
  }

  @Test
  @Order(5)
  void shouldDeleteCommentFromCommentsService() {
    // Create a comment to delete
    Map<String, String> request = new HashMap<>();
    request.put("body", "Comment to be deleted");
    request.put("userId", "user-2");
    request.put("articleId", "article-2");

    Response response =
        given()
            .baseUri(commentsServiceUrl)
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/comments")
            .then()
            .statusCode(201)
            .extract()
            .response();

    String commentIdToDelete = response.jsonPath().getString("id");

    // Delete the comment
    given()
        .baseUri(commentsServiceUrl)
        .when()
        .delete("/api/comments/" + commentIdToDelete)
        .then()
        .statusCode(204);

    // Verify it's gone
    given()
        .baseUri(commentsServiceUrl)
        .when()
        .get("/api/comments/" + commentIdToDelete)
        .then()
        .statusCode(404);
  }

  @Test
  @Order(6)
  void shouldReturn404ForNonExistentComment() {
    given()
        .baseUri(commentsServiceUrl)
        .when()
        .get("/api/comments/non-existent-id")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(7)
  void shouldReturnEmptyListForUnknownArticle() {
    given()
        .baseUri(commentsServiceUrl)
        .when()
        .get("/api/comments/article/non-existent-article")
        .then()
        .statusCode(200)
        .body("size()", equalTo(0));
  }
}
