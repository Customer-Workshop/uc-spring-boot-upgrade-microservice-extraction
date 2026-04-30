package io.spring.integration;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withCreatedEntity;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.spring.core.comment.Comment;
import io.spring.infrastructure.service.CommentResponse;
import io.spring.infrastructure.service.CommentServiceClient;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class CommentServiceClientTest {

  private RestTemplate restTemplate;
  private MockRestServiceServer mockServer;
  private CommentServiceClient client;

  @BeforeEach
  void setUp() {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    client = new CommentServiceClient(restTemplate, "http://comments-service:8081");
  }

  @Test
  void shouldCreateComment() {
    String responseBody =
        "{\"id\":\"comment-123\",\"body\":\"Test\",\"userId\":\"user-1\","
            + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
            + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withCreatedEntity(URI.create("http://comments-service:8081/api/comments/comment-123"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody));

    Comment comment = new Comment("Test", "user-1", "article-1");
    client.save(comment);

    Assertions.assertEquals("comment-123", comment.getId());
    mockServer.verify();
  }

  @Test
  void shouldGetCommentsByArticleId() {
    String responseBody =
        "[{\"id\":\"c1\",\"body\":\"Comment 1\",\"userId\":\"user-1\","
            + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
            + "\"updatedAt\":\"2026-01-01T00:00:00Z\"},"
            + "{\"id\":\"c2\",\"body\":\"Comment 2\",\"userId\":\"user-2\","
            + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-02T00:00:00Z\","
            + "\"updatedAt\":\"2026-01-02T00:00:00Z\"}]";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments?articleId=article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

    List<CommentResponse> comments = client.findByArticleId("article-1");

    Assertions.assertEquals(2, comments.size());
    Assertions.assertEquals("c1", comments.get(0).getId());
    Assertions.assertEquals("c2", comments.get(1).getId());
    mockServer.verify();
  }

  @Test
  void shouldGetCommentById() {
    String responseBody =
        "{\"id\":\"c1\",\"body\":\"Comment 1\",\"userId\":\"user-1\","
            + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
            + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/c1?articleId=article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

    Optional<Comment> comment = client.findById("article-1", "c1");

    Assertions.assertTrue(comment.isPresent());
    Assertions.assertEquals("c1", comment.get().getId());
    Assertions.assertEquals("Comment 1", comment.get().getBody());
    mockServer.verify();
  }

  @Test
  void shouldFindResponseById() {
    String responseBody =
        "{\"id\":\"c1\",\"body\":\"Comment 1\",\"userId\":\"user-1\","
            + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
            + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/c1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

    Optional<CommentResponse> response = client.findResponseById("c1");

    Assertions.assertTrue(response.isPresent());
    Assertions.assertEquals("c1", response.get().getId());
    mockServer.verify();
  }

  @Test
  void shouldDeleteComment() {
    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/c1"))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withNoContent());

    Comment comment = new Comment("Test", "user-1", "article-1");
    comment.setId("c1");
    client.remove(comment);

    mockServer.verify();
  }

  @Test
  void shouldReturnEmptyForNonexistentComment() {
    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/nonexistent"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess().body("").contentType(MediaType.APPLICATION_JSON));

    Optional<CommentResponse> response = client.findResponseById("nonexistent");

    mockServer.verify();
  }
}
