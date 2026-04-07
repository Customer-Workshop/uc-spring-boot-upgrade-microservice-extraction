package io.spring.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.JsonNode;
import io.spring.infrastructure.service.CommentServiceClient;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests verifying communication between the monolith and the Comments microservice.
 * Uses MockRestServiceServer to simulate the microservice HTTP responses.
 */
public class CommentServiceClientTest {

  private RestTemplate restTemplate;
  private MockRestServiceServer mockServer;
  private CommentServiceClient commentServiceClient;

  private static final String COMMENTS_SERVICE_URL = "http://localhost:8081";

  @BeforeEach
  public void setUp() {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    commentServiceClient = new CommentServiceClient(restTemplate, COMMENTS_SERVICE_URL);
  }

  @Test
  public void should_create_comment_via_microservice() {
    String articleId = "article-123";
    String responseJson =
        "{\"comment\":{\"id\":\"comment-1\",\"body\":\"test body\","
            + "\"articleId\":\"article-123\",\"userId\":\"user-1\"}}";

    mockServer
        .expect(requestTo(COMMENTS_SERVICE_URL + "/api/articles/" + articleId + "/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.CREATED).body(responseJson).contentType(MediaType.APPLICATION_JSON));

    Optional<JsonNode> result =
        commentServiceClient.createComment(articleId, "test body", "user-1");

    assertTrue(result.isPresent());
    assertEquals("comment-1", result.get().get("comment").get("id").asText());
    assertEquals("test body", result.get().get("comment").get("body").asText());

    mockServer.verify();
  }

  @Test
  public void should_get_comments_by_article_id_via_microservice() {
    String articleId = "article-123";
    String responseJson =
        "{\"comments\":[{\"id\":\"c1\",\"body\":\"comment 1\"},"
            + "{\"id\":\"c2\",\"body\":\"comment 2\"}]}";

    mockServer
        .expect(requestTo(COMMENTS_SERVICE_URL + "/api/articles/" + articleId + "/comments"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    Optional<JsonNode> result = commentServiceClient.getCommentsByArticleId(articleId);

    assertTrue(result.isPresent());
    assertEquals(2, result.get().get("comments").size());

    mockServer.verify();
  }

  @Test
  public void should_get_single_comment_via_microservice() {
    String articleId = "article-123";
    String commentId = "comment-1";
    String responseJson =
        "{\"comment\":{\"id\":\"comment-1\",\"body\":\"test body\"}}";

    mockServer
        .expect(
            requestTo(
                COMMENTS_SERVICE_URL
                    + "/api/articles/"
                    + articleId
                    + "/comments/"
                    + commentId))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    Optional<JsonNode> result = commentServiceClient.getComment(articleId, commentId);

    assertTrue(result.isPresent());
    assertEquals("comment-1", result.get().get("comment").get("id").asText());

    mockServer.verify();
  }

  @Test
  public void should_delete_comment_via_microservice() {
    String articleId = "article-123";
    String commentId = "comment-1";

    mockServer
        .expect(
            requestTo(
                COMMENTS_SERVICE_URL
                    + "/api/articles/"
                    + articleId
                    + "/comments/"
                    + commentId))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withNoContent());

    boolean result = commentServiceClient.deleteComment(articleId, commentId);

    assertTrue(result);
    mockServer.verify();
  }

  @Test
  public void should_return_empty_when_microservice_returns_error_on_create() {
    String articleId = "article-123";

    mockServer
        .expect(requestTo(COMMENTS_SERVICE_URL + "/api/articles/" + articleId + "/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withServerError());

    Optional<JsonNode> result =
        commentServiceClient.createComment(articleId, "test body", "user-1");

    assertFalse(result.isPresent());
    mockServer.verify();
  }

  @Test
  public void should_return_empty_when_microservice_returns_error_on_get() {
    String articleId = "article-123";

    mockServer
        .expect(requestTo(COMMENTS_SERVICE_URL + "/api/articles/" + articleId + "/comments"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withServerError());

    Optional<JsonNode> result = commentServiceClient.getCommentsByArticleId(articleId);

    assertFalse(result.isPresent());
    mockServer.verify();
  }

  @Test
  public void should_return_false_when_microservice_returns_error_on_delete() {
    String articleId = "article-123";
    String commentId = "comment-1";

    mockServer
        .expect(
            requestTo(
                COMMENTS_SERVICE_URL
                    + "/api/articles/"
                    + articleId
                    + "/comments/"
                    + commentId))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withServerError());

    boolean result = commentServiceClient.deleteComment(articleId, commentId);

    assertFalse(result);
    mockServer.verify();
  }
}
