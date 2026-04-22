package io.spring.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentsIntegrationTest {

  private static final String COMMENTS_SERVICE_URL =
      System.getenv().getOrDefault("COMMENTS_SERVICE_URL", "http://localhost:8081");

  private static HttpClient httpClient;
  private static ObjectMapper objectMapper;
  private static String createdCommentId;

  private static final String TEST_ARTICLE_ID = "integration-test-article-1";
  private static final String TEST_USER_ID = "integration-test-user-1";

  @BeforeAll
  static void setUp() {
    httpClient = HttpClient.newHttpClient();
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JodaModule());
  }

  @Test
  @Order(1)
  void commentsServiceHealthCheck() throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(COMMENTS_SERVICE_URL + "/actuator/health"))
            .GET()
            .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());
    JsonNode body = objectMapper.readTree(response.body());
    assertEquals("UP", body.get("status").asText());
  }

  @Test
  @Order(2)
  void shouldCreateCommentViaCommentsService() throws Exception {
    Map<String, String> payload = Map.of("body", "Integration test comment", "userId", TEST_USER_ID);
    String json = objectMapper.writeValueAsString(payload);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(
                URI.create(
                    COMMENTS_SERVICE_URL
                        + "/api/articles/"
                        + TEST_ARTICLE_ID
                        + "/comments"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(201, response.statusCode());

    JsonNode body = objectMapper.readTree(response.body());
    JsonNode comment = body.get("comment");
    assertNotNull(comment);
    assertNotNull(comment.get("id").asText());
    assertEquals("Integration test comment", comment.get("body").asText());

    createdCommentId = comment.get("id").asText();
    assertFalse(createdCommentId.isEmpty());
  }

  @Test
  @Order(3)
  void shouldGetCommentsFromCommentsService() throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(
                URI.create(
                    COMMENTS_SERVICE_URL
                        + "/api/articles/"
                        + TEST_ARTICLE_ID
                        + "/comments"))
            .GET()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());

    JsonNode body = objectMapper.readTree(response.body());
    JsonNode comments = body.get("comments");
    assertNotNull(comments);
    assertTrue(comments.isArray());
    assertTrue(comments.size() > 0);

    boolean found = false;
    for (JsonNode c : comments) {
      if (c.get("body").asText().equals("Integration test comment")) {
        found = true;
        break;
      }
    }
    assertTrue(found, "Created comment should appear in the list");
  }

  @Test
  @Order(4)
  void shouldGetSingleCommentFromCommentsService() throws Exception {
    assertNotNull(createdCommentId, "Comment should have been created in previous test");

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(
                URI.create(
                    COMMENTS_SERVICE_URL
                        + "/api/articles/"
                        + TEST_ARTICLE_ID
                        + "/comments/"
                        + createdCommentId))
            .GET()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, response.statusCode());

    JsonNode body = objectMapper.readTree(response.body());
    JsonNode comment = body.get("comment");
    assertNotNull(comment);
    assertEquals(createdCommentId, comment.get("id").asText());
    assertEquals("Integration test comment", comment.get("body").asText());
  }

  @Test
  @Order(5)
  void shouldDeleteCommentFromCommentsService() throws Exception {
    assertNotNull(createdCommentId, "Comment should have been created in previous test");

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(
                URI.create(
                    COMMENTS_SERVICE_URL
                        + "/api/articles/"
                        + TEST_ARTICLE_ID
                        + "/comments/"
                        + createdCommentId))
            .DELETE()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(204, response.statusCode());
  }

  @Test
  @Order(6)
  void shouldNotFindDeletedComment() throws Exception {
    assertNotNull(createdCommentId, "Comment should have been created in previous test");

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(
                URI.create(
                    COMMENTS_SERVICE_URL
                        + "/api/articles/"
                        + TEST_ARTICLE_ID
                        + "/comments/"
                        + createdCommentId))
            .GET()
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(404, response.statusCode());
  }

  @Test
  @Order(7)
  void shouldCreateMultipleCommentsAndListThem() throws Exception {
    String articleId = "integration-test-article-multi";

    for (int i = 1; i <= 3; i++) {
      Map<String, String> payload =
          Map.of("body", "Multi comment " + i, "userId", TEST_USER_ID);
      String json = objectMapper.writeValueAsString(payload);

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(
                  URI.create(
                      COMMENTS_SERVICE_URL + "/api/articles/" + articleId + "/comments"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(json))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      assertEquals(201, response.statusCode());
    }

    HttpRequest listRequest =
        HttpRequest.newBuilder()
            .uri(
                URI.create(
                    COMMENTS_SERVICE_URL + "/api/articles/" + articleId + "/comments"))
            .GET()
            .build();

    HttpResponse<String> listResponse =
        httpClient.send(listRequest, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, listResponse.statusCode());

    JsonNode body = objectMapper.readTree(listResponse.body());
    JsonNode comments = body.get("comments");
    assertEquals(3, comments.size(), "Should have exactly 3 comments for this article");
  }
}
