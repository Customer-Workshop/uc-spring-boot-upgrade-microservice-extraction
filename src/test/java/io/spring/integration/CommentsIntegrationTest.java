package io.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.infrastructure.service.comments.CommentServiceClient;
import io.spring.infrastructure.service.comments.CommentServiceResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests that verify the monolith communicates correctly with the Comments microservice
 * via HTTP. These tests require the comments-service to be running on the configured URL (default:
 * http://localhost:8081).
 *
 * <p>Run with: ./gradlew test --tests "io.spring.integration.*"
 *
 * <p>Before running, start the comments service: cd comments-service && ./gradlew bootRun
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class CommentsIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private CommentServiceClient commentServiceClient;

  @Autowired private ObjectMapper objectMapper;

  private String monolithBaseUrl() {
    return "http://localhost:" + port;
  }

  @Test
  void commentServiceClient_shouldCreateAndRetrieveComments() {
    String articleId = "integration-test-article-1";

    // Create a comment via the service client
    CommentServiceResponse created =
        commentServiceClient.createComment(articleId, "Integration test comment", "user-1");

    assertThat(created).isNotNull();
    assertThat(created.getId()).isNotNull();
    assertThat(created.getBody()).isEqualTo("Integration test comment");
    assertThat(created.getArticleId()).isEqualTo(articleId);
    assertThat(created.getUserId()).isEqualTo("user-1");

    // Retrieve comments for the article
    List<CommentServiceResponse> comments =
        commentServiceClient.getCommentsByArticleId(articleId);

    assertThat(comments).isNotEmpty();
    assertThat(comments).anyMatch(c -> c.getId().equals(created.getId()));
  }

  @Test
  void commentServiceClient_shouldGetCommentById() {
    String articleId = "integration-test-article-2";

    CommentServiceResponse created =
        commentServiceClient.createComment(articleId, "Get by ID test", "user-2");

    var fetched = commentServiceClient.getComment(articleId, created.getId());

    assertThat(fetched).isPresent();
    assertThat(fetched.get().getBody()).isEqualTo("Get by ID test");
    assertThat(fetched.get().getUserId()).isEqualTo("user-2");
  }

  @Test
  void commentServiceClient_shouldDeleteComment() {
    String articleId = "integration-test-article-3";

    CommentServiceResponse created =
        commentServiceClient.createComment(articleId, "To be deleted", "user-1");

    // Delete the comment
    commentServiceClient.deleteComment(articleId, created.getId());

    // Verify it's gone
    var fetched = commentServiceClient.getComment(articleId, created.getId());
    assertThat(fetched).isEmpty();
  }

  @Test
  void commentServiceClient_shouldReturnEmptyForNonexistentComment() {
    var fetched = commentServiceClient.getComment("nonexistent-article", "nonexistent-id");
    assertThat(fetched).isEmpty();
  }

  @Test
  void commentServiceClient_shouldReturnEmptyListForArticleWithNoComments() {
    List<CommentServiceResponse> comments =
        commentServiceClient.getCommentsByArticleId("no-comments-article");
    assertThat(comments).isEmpty();
  }

  @Test
  void monolith_shouldProxyCommentsViaAuthenticatedEndpoint() throws Exception {
    // Register a user first
    String registerBody =
        "{\"user\":{\"email\":\"integration@test.com\",\"username\":\"integrationuser\",\"password\":\"password123\"}}";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> registerResponse =
        restTemplate.exchange(
            monolithBaseUrl() + "/users",
            HttpMethod.POST,
            new HttpEntity<>(registerBody, headers),
            String.class);

    // If registration succeeds, get the token
    if (registerResponse.getStatusCode() == HttpStatus.OK
        || registerResponse.getStatusCode() == HttpStatus.CREATED) {
      JsonNode userNode = objectMapper.readTree(registerResponse.getBody());
      String token = userNode.path("user").path("token").asText();

      assertThat(token).isNotEmpty();

      // Create an article first
      String articleBody =
          "{\"article\":{\"title\":\"Integration Test Article\","
              + "\"description\":\"Test desc\","
              + "\"body\":\"Test body\","
              + "\"tagList\":[\"test\"]}}";

      HttpHeaders authHeaders = new HttpHeaders();
      authHeaders.setContentType(MediaType.APPLICATION_JSON);
      authHeaders.set("Authorization", "Token " + token);

      ResponseEntity<String> articleResponse =
          restTemplate.exchange(
              monolithBaseUrl() + "/articles",
              HttpMethod.POST,
              new HttpEntity<>(articleBody, authHeaders),
              String.class);

      if (articleResponse.getStatusCode() == HttpStatus.OK
          || articleResponse.getStatusCode() == HttpStatus.CREATED) {
        JsonNode articleNode = objectMapper.readTree(articleResponse.getBody());
        String slug = articleNode.path("article").path("slug").asText();

        // Create a comment via the monolith endpoint
        String commentBody = "{\"comment\":{\"body\":\"End-to-end comment test\"}}";

        ResponseEntity<String> commentResponse =
            restTemplate.exchange(
                monolithBaseUrl() + "/articles/" + slug + "/comments",
                HttpMethod.POST,
                new HttpEntity<>(commentBody, authHeaders),
                String.class);

        assertThat(commentResponse.getStatusCode().value()).isEqualTo(201);

        JsonNode commentNode = objectMapper.readTree(commentResponse.getBody());
        assertThat(commentNode.path("comment").path("body").asText())
            .isEqualTo("End-to-end comment test");
        assertThat(commentNode.path("comment").path("id").asText()).isNotEmpty();

        // Fetch comments via monolith
        ResponseEntity<String> getCommentsResponse =
            restTemplate.exchange(
                monolithBaseUrl() + "/articles/" + slug + "/comments",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders),
                String.class);

        assertThat(getCommentsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode commentsNode = objectMapper.readTree(getCommentsResponse.getBody());
        assertThat(commentsNode.path("comments").size()).isGreaterThanOrEqualTo(1);
      }
    }
  }
}
