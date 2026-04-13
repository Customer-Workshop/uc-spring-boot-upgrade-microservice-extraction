package io.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.spring.infrastructure.service.CommentServiceClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests that verify the monolith can communicate correctly with the comments
 * microservice via HTTP. These tests require the comments microservice to be running on
 * localhost:8081 (e.g., via docker-compose).
 *
 * <p>Run with: ./gradlew test --tests "io.spring.integration.*"
 *
 * <p>Or start the comments service first, then run these tests.
 */
class CommentsIntegrationTest {

  private RestTemplate restTemplate;
  private String commentsServiceUrl;

  @BeforeEach
  void setUp() {
    restTemplate = new RestTemplate();
    commentsServiceUrl = System.getProperty("comments.service.url", "http://localhost:8081");
  }

  @Test
  void shouldCreateCommentViaCommentsService() {
    Map<String, String> request = new HashMap<>();
    request.put("body", "Integration test comment");
    request.put("userId", "user-integration-1");
    request.put("articleId", "article-integration-1");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            commentsServiceUrl + "/api/comments",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<Map<String, Object>>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).containsKey("comment");

    @SuppressWarnings("unchecked")
    Map<String, Object> comment = (Map<String, Object>) response.getBody().get("comment");
    assertThat(comment.get("body")).isEqualTo("Integration test comment");
    assertThat(comment.get("userId")).isEqualTo("user-integration-1");
    assertThat(comment.get("articleId")).isEqualTo("article-integration-1");
    assertThat(comment.get("id")).isNotNull();
  }

  @Test
  void shouldRetrieveCommentsByArticleId() {
    String articleId = "article-retrieve-" + System.currentTimeMillis();
    createComment("Comment 1", "user-1", articleId);
    createComment("Comment 2", "user-2", articleId);

    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            commentsServiceUrl + "/api/comments/article/" + articleId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> comments =
        (List<Map<String, Object>>) response.getBody().get("comments");
    assertThat(comments).hasSize(2);
  }

  @Test
  void shouldDeleteComment() {
    String articleId = "article-delete-" + System.currentTimeMillis();
    String commentId = createComment("To be deleted", "user-1", articleId);

    restTemplate.delete(commentsServiceUrl + "/api/comments/" + commentId);

    try {
      restTemplate.exchange(
          commentsServiceUrl + "/api/comments/" + commentId,
          HttpMethod.GET,
          null,
          new ParameterizedTypeReference<Map<String, Object>>() {});
      assertThat(false).as("Should have thrown 404").isTrue();
    } catch (HttpClientErrorException.NotFound e) {
      assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
  }

  @Test
  void shouldGetCommentByArticleAndId() {
    String articleId = "article-getbyid-" + System.currentTimeMillis();
    String commentId = createComment("Specific comment", "user-1", articleId);

    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            commentsServiceUrl + "/api/comments/article/" + articleId + "/" + commentId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    @SuppressWarnings("unchecked")
    Map<String, Object> comment = (Map<String, Object>) response.getBody().get("comment");
    assertThat(comment.get("body")).isEqualTo("Specific comment");
  }

  @Test
  void shouldVerifyMonolithCanUseCommentServiceClient() {
    RestTemplate rt = new RestTemplate();
    CommentServiceClient client = new CommentServiceClient(rt, commentsServiceUrl);

    io.spring.core.comment.Comment comment =
        new io.spring.core.comment.Comment(
            "Client test comment", "user-client-1", "article-client-1");
    client.save(comment);

    List<Map<String, Object>> rawComments = client.findRawByArticleId("article-client-1");
    assertThat(rawComments).isNotEmpty();
    assertThat(rawComments.get(0).get("body")).isEqualTo("Client test comment");
  }

  private String createComment(String body, String userId, String articleId) {
    Map<String, String> request = new HashMap<>();
    request.put("body", body);
    request.put("userId", userId);
    request.put("articleId", articleId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            commentsServiceUrl + "/api/comments",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<Map<String, Object>>() {});

    @SuppressWarnings("unchecked")
    Map<String, Object> comment = (Map<String, Object>) response.getBody().get("comment");
    return (String) comment.get("id");
  }
}
