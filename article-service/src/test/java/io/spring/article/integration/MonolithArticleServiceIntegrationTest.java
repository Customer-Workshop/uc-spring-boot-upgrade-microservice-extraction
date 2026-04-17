package io.spring.article.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.article.api.dto.CreateArticleRequest;
import io.spring.article.api.dto.CreateCommentRequest;
import io.spring.article.api.dto.UpdateArticleRequest;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests that verify the article microservice API contract works correctly. These tests
 * simulate how the monolith would communicate with the article-service over HTTP, validating the
 * full request/response cycle including serialization, database persistence, and error handling.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MonolithArticleServiceIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private ObjectMapper objectMapper;

  private String baseUrl() {
    return "http://localhost:" + port + "/api";
  }

  @Test
  @Order(1)
  void monolithCanCreateArticleInMicroservice() throws Exception {
    CreateArticleRequest request =
        CreateArticleRequest.builder()
            .title("Integration Test Article")
            .description("Testing monolith to microservice communication")
            .body("This article was created via the article-service API")
            .tagList(List.of("integration", "microservices"))
            .userId("monolith-user-001")
            .build();

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl() + "/articles", request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.get("title").asText()).isEqualTo("Integration Test Article");
    assertThat(body.get("slug").asText()).isEqualTo("integration-test-article");
    assertThat(body.get("userId").asText()).isEqualTo("monolith-user-001");
    assertThat(body.get("id").asText()).isNotEmpty();
  }

  @Test
  @Order(2)
  void monolithCanRetrieveArticleFromMicroservice() throws Exception {
    CreateArticleRequest request =
        CreateArticleRequest.builder()
            .title("Retrievable Article")
            .description("Can be retrieved")
            .body("Content here")
            .tagList(List.of("test"))
            .userId("monolith-user-002")
            .build();

    restTemplate.postForEntity(baseUrl() + "/articles", request, String.class);

    ResponseEntity<String> response =
        restTemplate.getForEntity(baseUrl() + "/articles/retrievable-article", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.get("title").asText()).isEqualTo("Retrievable Article");
    assertThat(body.get("userId").asText()).isEqualTo("monolith-user-002");
  }

  @Test
  @Order(3)
  void monolithCanUpdateArticleInMicroservice() throws Exception {
    CreateArticleRequest createRequest =
        CreateArticleRequest.builder()
            .title("Updatable Article")
            .description("Original description")
            .body("Original body")
            .tagList(List.of())
            .userId("monolith-user-003")
            .build();

    restTemplate.postForEntity(baseUrl() + "/articles", createRequest, String.class);

    UpdateArticleRequest updateRequest =
        UpdateArticleRequest.builder()
            .title("Updated Via Monolith")
            .description("Updated description from monolith")
            .body("Updated body content")
            .build();

    ResponseEntity<String> response =
        restTemplate.exchange(
            baseUrl() + "/articles/updatable-article",
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode body = objectMapper.readTree(response.getBody());
    assertThat(body.get("title").asText()).isEqualTo("Updated Via Monolith");
    assertThat(body.get("description").asText()).isEqualTo("Updated description from monolith");
  }

  @Test
  @Order(4)
  void monolithCanDeleteArticleFromMicroservice() {
    CreateArticleRequest request =
        CreateArticleRequest.builder()
            .title("Deletable Integration Article")
            .description("Will be deleted")
            .body("Temporary content")
            .tagList(List.of())
            .userId("monolith-user-004")
            .build();

    restTemplate.postForEntity(baseUrl() + "/articles", request, String.class);

    // Delete the article
    ResponseEntity<Void> deleteResponse =
        restTemplate.exchange(
            baseUrl() + "/articles/deletable-integration-article",
            HttpMethod.DELETE,
            null,
            Void.class);

    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    // Verify it no longer exists
    ResponseEntity<String> getResponse =
        restTemplate.getForEntity(
            baseUrl() + "/articles/deletable-integration-article", String.class);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @Order(5)
  void monolithCanAddCommentsToArticleInMicroservice() throws Exception {
    CreateArticleRequest articleRequest =
        CreateArticleRequest.builder()
            .title("Article With Comments Integration")
            .description("Has comments")
            .body("Content")
            .tagList(List.of())
            .userId("monolith-user-005")
            .build();

    restTemplate.postForEntity(baseUrl() + "/articles", articleRequest, String.class);

    CreateCommentRequest commentRequest =
        CreateCommentRequest.builder()
            .body("Comment from monolith service")
            .userId("monolith-commenter-001")
            .build();

    ResponseEntity<String> commentResponse =
        restTemplate.postForEntity(
            baseUrl() + "/articles/article-with-comments-integration/comments",
            commentRequest,
            String.class);

    assertThat(commentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode commentBody = objectMapper.readTree(commentResponse.getBody());
    assertThat(commentBody.get("body").asText()).isEqualTo("Comment from monolith service");

    ResponseEntity<String> commentsResponse =
        restTemplate.getForEntity(
            baseUrl() + "/articles/article-with-comments-integration/comments", String.class);

    assertThat(commentsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode comments = objectMapper.readTree(commentsResponse.getBody());
    assertThat(comments.isArray()).isTrue();
    assertThat(comments.size()).isEqualTo(1);
    assertThat(comments.get(0).get("body").asText()).isEqualTo("Comment from monolith service");
  }

  @Test
  @Order(6)
  void microserviceReturns404ForNonExistentArticle() {
    ResponseEntity<String> response =
        restTemplate.getForEntity(baseUrl() + "/articles/does-not-exist", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @Order(7)
  void microserviceValidatesArticleCreationRequest() {
    // Send a request with missing required fields
    CreateArticleRequest invalidRequest =
        CreateArticleRequest.builder()
            .title("")
            .description("")
            .body("")
            .userId("")
            .build();

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl() + "/articles", invalidRequest, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
