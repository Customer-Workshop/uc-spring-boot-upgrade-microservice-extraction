package io.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withCreatedEntity;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.spring.core.comment.Comment;
import io.spring.infrastructure.service.CommentServiceClient;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests that verify the monolith communicates correctly with the comments microservice
 * via HTTP. Uses MockRestServiceServer to simulate the comments microservice endpoints.
 */
public class CommentServiceIntegrationTest {

  private CommentServiceClient commentServiceClient;
  private MockRestServiceServer mockServer;
  private RestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    commentServiceClient = new CommentServiceClient(restTemplate, "http://localhost:8081");
  }

  @Test
  void shouldCreateCommentViaHttpToMicroservice() throws Exception {
    String responseJson =
        "{\"id\":\"comment-123\",\"body\":\"Great article!\","
            + "\"userId\":\"user-1\",\"articleId\":\"article-1\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"updatedAt\":\"2026-01-01T00:00:00Z\"}";

    mockServer
        .expect(requestTo("http://localhost:8081/api/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withCreatedEntity(URI.create("http://localhost:8081/api/comments/comment-123"))
                .body(responseJson)
                .contentType(MediaType.APPLICATION_JSON));

    Comment comment = new Comment("Great article!", "user-1", "article-1");
    commentServiceClient.save(comment);

    mockServer.verify();
  }

  @Test
  void shouldFetchCommentsByArticleIdFromMicroservice() throws Exception {
    String responseJson =
        "[{\"id\":\"comment-1\",\"body\":\"Comment 1\","
            + "\"userId\":\"user-1\",\"articleId\":\"article-1\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"updatedAt\":\"2026-01-01T00:00:00Z\"},"
            + "{\"id\":\"comment-2\",\"body\":\"Comment 2\","
            + "\"userId\":\"user-2\",\"articleId\":\"article-1\","
            + "\"createdAt\":\"2026-01-02T00:00:00Z\",\"updatedAt\":\"2026-01-02T00:00:00Z\"}]";

    mockServer
        .expect(requestTo("http://localhost:8081/api/comments?articleId=article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    List<Comment> comments = commentServiceClient.findByArticleId("article-1");

    assertThat(comments).hasSize(2);
    assertThat(comments.get(0).getBody()).isEqualTo("Comment 1");
    assertThat(comments.get(1).getBody()).isEqualTo("Comment 2");
    mockServer.verify();
  }

  @Test
  void shouldFetchCommentByIdAndArticleIdFromMicroservice() throws Exception {
    String responseJson =
        "{\"id\":\"comment-1\",\"body\":\"Test comment\","
            + "\"userId\":\"user-1\",\"articleId\":\"article-1\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"updatedAt\":\"2026-01-01T00:00:00Z\"}";

    mockServer
        .expect(requestTo("http://localhost:8081/api/comments/comment-1?articleId=article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    Optional<Comment> result = commentServiceClient.findById("article-1", "comment-1");

    assertThat(result).isPresent();
    assertThat(result.get().getBody()).isEqualTo("Test comment");
    assertThat(result.get().getArticleId()).isEqualTo("article-1");
    mockServer.verify();
  }

  @Test
  void shouldFetchCommentByIdFromMicroservice() throws Exception {
    String responseJson =
        "{\"id\":\"comment-1\",\"body\":\"Test comment\","
            + "\"userId\":\"user-1\",\"articleId\":\"article-1\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"updatedAt\":\"2026-01-01T00:00:00Z\"}";

    mockServer
        .expect(requestTo("http://localhost:8081/api/comments/comment-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    Optional<Comment> result = commentServiceClient.findByCommentId("comment-1");

    assertThat(result).isPresent();
    assertThat(result.get().getBody()).isEqualTo("Test comment");
    mockServer.verify();
  }

  @Test
  void shouldDeleteCommentViaHttpToMicroservice() throws Exception {
    mockServer
        .expect(requestTo("http://localhost:8081/api/comments/comment-1"))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withNoContent());

    Comment comment = new Comment("body", "user-1", "article-1");
    java.lang.reflect.Field idField = Comment.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(comment, "comment-1");

    commentServiceClient.remove(comment);

    mockServer.verify();
  }
}
