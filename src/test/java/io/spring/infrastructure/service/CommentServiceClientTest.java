package io.spring.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class CommentServiceClientTest {

  private CommentServiceClient client;
  private MockRestServiceServer mockServer;
  private RestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    client = new CommentServiceClient(restTemplate, "http://comments-service:8081");
  }

  @Test
  void shouldCreateComment() {
    String responseJson =
        "{\"id\":\"comment-123\",\"body\":\"Hello\",\"userId\":\"user-1\","
            + "\"articleId\":\"article-1\",\"createdAt\":\"2024-01-01T00:00:00Z\","
            + "\"updatedAt\":\"2024-01-01T00:00:00Z\"}";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    CommentResponse response = client.createComment("Hello", "user-1", "article-1");

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo("comment-123");
    assertThat(response.getBody()).isEqualTo("Hello");
    assertThat(response.getUserId()).isEqualTo("user-1");
    assertThat(response.getArticleId()).isEqualTo("article-1");
    mockServer.verify();
  }

  @Test
  void shouldGetCommentsByArticleId() {
    String responseJson =
        "[{\"id\":\"c1\",\"body\":\"Comment 1\",\"userId\":\"user-1\","
            + "\"articleId\":\"article-1\",\"createdAt\":\"2024-01-01T00:00:00Z\","
            + "\"updatedAt\":\"2024-01-01T00:00:00Z\"},"
            + "{\"id\":\"c2\",\"body\":\"Comment 2\",\"userId\":\"user-2\","
            + "\"articleId\":\"article-1\",\"createdAt\":\"2024-01-01T00:00:00Z\","
            + "\"updatedAt\":\"2024-01-01T00:00:00Z\"}]";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments?articleId=article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    List<CommentResponse> comments = client.getCommentsByArticleId("article-1");

    assertThat(comments).hasSize(2);
    assertThat(comments.get(0).getId()).isEqualTo("c1");
    assertThat(comments.get(1).getId()).isEqualTo("c2");
    mockServer.verify();
  }

  @Test
  void shouldGetCommentById() {
    String responseJson =
        "{\"id\":\"c1\",\"body\":\"Found comment\",\"userId\":\"user-1\","
            + "\"articleId\":\"article-1\",\"createdAt\":\"2024-01-01T00:00:00Z\","
            + "\"updatedAt\":\"2024-01-01T00:00:00Z\"}";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/c1?articleId=article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    Optional<CommentResponse> response = client.getCommentById("c1", "article-1");

    assertThat(response).isPresent();
    assertThat(response.get().getBody()).isEqualTo("Found comment");
    mockServer.verify();
  }

  @Test
  void shouldReturnEmptyForNonExistentComment() {
    mockServer
        .expect(
            requestTo("http://comments-service:8081/api/comments/nonexistent?articleId=article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    Optional<CommentResponse> response = client.getCommentById("nonexistent", "article-1");

    assertThat(response).isEmpty();
    mockServer.verify();
  }

  @Test
  void shouldDeleteComment() {
    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/c1?articleId=article-1"))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withSuccess());

    client.deleteComment("c1", "article-1");

    mockServer.verify();
  }
}
