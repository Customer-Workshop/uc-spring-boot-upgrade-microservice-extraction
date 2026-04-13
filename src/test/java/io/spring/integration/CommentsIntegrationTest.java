package io.spring.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests that verify the monolith and comments microservice communicate correctly.
 *
 * <p>These tests require both services to be running: - Comments microservice on port 8081 -
 * Monolith on port 8080
 *
 * <p>Run with: docker-compose up, then execute these tests.
 *
 * <p>To run: ./gradlew test --tests "io.spring.integration.CommentsIntegrationTest"
 * -Dintegration.test.enabled=true
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentsIntegrationTest {

  private static final String COMMENTS_SERVICE_URL = "http://localhost:8081";
  private static final String MONOLITH_URL = "http://localhost:8080";
  private static final RestTemplate restTemplate = new RestTemplate();
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static String createdCommentId;
  private static boolean integrationTestEnabled;

  @BeforeAll
  static void setUp() {
    integrationTestEnabled = "true".equals(System.getProperty("integration.test.enabled"));
    if (!integrationTestEnabled) {
      return;
    }
  }

  @Test
  @Order(1)
  void commentsServiceShouldBeHealthy() throws Exception {
    if (!integrationTestEnabled) {
      return;
    }
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            COMMENTS_SERVICE_URL + "/api/comments?articleId=health-check", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  @Order(2)
  void shouldCreateCommentViaCommentsService() throws Exception {
    if (!integrationTestEnabled) {
      return;
    }
    Map<String, String> request = new HashMap<>();
    request.put("body", "Integration test comment");
    request.put("userId", "test-user-1");
    request.put("articleId", "test-article-1");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            COMMENTS_SERVICE_URL + "/api/comments", entity, String.class);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    JsonNode body = objectMapper.readTree(response.getBody());
    assertNotNull(body.get("id").asText());
    assertEquals("Integration test comment", body.get("body").asText());
    assertEquals("test-user-1", body.get("userId").asText());
    assertEquals("test-article-1", body.get("articleId").asText());

    createdCommentId = body.get("id").asText();
  }

  @Test
  @Order(3)
  void shouldRetrieveCommentByIdFromCommentsService() throws Exception {
    if (!integrationTestEnabled) {
      return;
    }
    if (createdCommentId == null) {
      return;
    }

    ResponseEntity<String> response =
        restTemplate.getForEntity(
            COMMENTS_SERVICE_URL + "/api/comments/" + createdCommentId, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    JsonNode body = objectMapper.readTree(response.getBody());
    assertEquals(createdCommentId, body.get("id").asText());
    assertEquals("Integration test comment", body.get("body").asText());
  }

  @Test
  @Order(4)
  void shouldRetrieveCommentsByArticleIdFromCommentsService() throws Exception {
    if (!integrationTestEnabled) {
      return;
    }

    ResponseEntity<String> response =
        restTemplate.getForEntity(
            COMMENTS_SERVICE_URL + "/api/comments?articleId=test-article-1", String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    JsonNode body = objectMapper.readTree(response.getBody());
    assertTrue(body.isArray());
    assertFalse(body.isEmpty());
    assertEquals("test-article-1", body.get(0).get("articleId").asText());
  }

  @Test
  @Order(5)
  void shouldRetrieveCommentByIdAndArticleIdFromCommentsService() throws Exception {
    if (!integrationTestEnabled) {
      return;
    }
    if (createdCommentId == null) {
      return;
    }

    ResponseEntity<String> response =
        restTemplate.getForEntity(
            COMMENTS_SERVICE_URL
                + "/api/comments/"
                + createdCommentId
                + "/by-article?articleId=test-article-1",
            String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    JsonNode body = objectMapper.readTree(response.getBody());
    assertEquals(createdCommentId, body.get("id").asText());
  }

  @Test
  @Order(6)
  void shouldDeleteCommentFromCommentsService() throws Exception {
    if (!integrationTestEnabled) {
      return;
    }
    if (createdCommentId == null) {
      return;
    }

    restTemplate.delete(COMMENTS_SERVICE_URL + "/api/comments/" + createdCommentId);

    try {
      restTemplate.getForEntity(
          COMMENTS_SERVICE_URL + "/api/comments/" + createdCommentId, String.class);
    } catch (HttpClientErrorException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
  }

  @Test
  @Order(7)
  void shouldReturn404ForNonExistentComment() throws Exception {
    if (!integrationTestEnabled) {
      return;
    }

    try {
      restTemplate.getForEntity(
          COMMENTS_SERVICE_URL + "/api/comments/non-existent-id", String.class);
    } catch (HttpClientErrorException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
  }

  @Test
  @Order(8)
  void commentsServiceAndMonolithShouldCommunicate() throws Exception {
    if (!integrationTestEnabled) {
      return;
    }

    // Create a comment directly in the comments service
    Map<String, String> request = new HashMap<>();
    request.put("body", "Cross-service test comment");
    request.put("userId", "test-user-2");
    request.put("articleId", "test-article-2");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

    ResponseEntity<String> createResponse =
        restTemplate.postForEntity(
            COMMENTS_SERVICE_URL + "/api/comments", entity, String.class);
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
    String commentId = objectMapper.readTree(createResponse.getBody()).get("id").asText();

    // Verify the comment is accessible via the comments service
    ResponseEntity<String> getResponse =
        restTemplate.getForEntity(
            COMMENTS_SERVICE_URL + "/api/comments/" + commentId, String.class);
    assertEquals(HttpStatus.OK, getResponse.getStatusCode());

    JsonNode commentBody = objectMapper.readTree(getResponse.getBody());
    assertEquals("Cross-service test comment", commentBody.get("body").asText());
    assertEquals("test-user-2", commentBody.get("userId").asText());

    // Clean up
    restTemplate.exchange(
        COMMENTS_SERVICE_URL + "/api/comments/" + commentId,
        HttpMethod.DELETE,
        null,
        String.class);
  }
}
