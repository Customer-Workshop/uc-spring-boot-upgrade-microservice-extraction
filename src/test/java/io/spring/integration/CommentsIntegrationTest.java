package io.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.infrastructure.service.comment.CommentServiceClient;
import io.spring.infrastructure.service.comment.CommentServiceClient.CommentResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class CommentsIntegrationTest {

  @Autowired private RestTemplate restTemplate;

  @Autowired private CommentServiceClient commentServiceClient;

  @Autowired private ObjectMapper objectMapper;

  private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
  }

  @Test
  void shouldCreateCommentViaHttpClient() throws Exception {
    String responseJson =
        "{\"comment\":{\"id\":\"test-comment-id\",\"body\":\"Test body\",\"userId\":\"user-1\",\"articleId\":\"article-1\",\"createdAt\":\"2024-01-01T00:00:00.000Z\",\"updatedAt\":\"2024-01-01T00:00:00.000Z\"}}";

    mockServer
        .expect(requestTo("http://localhost:8081/api/comments/articles/article-1"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    CommentResponse result = commentServiceClient.createComment("article-1", "Test body", "user-1");

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo("test-comment-id");
    assertThat(result.getBody()).isEqualTo("Test body");
    assertThat(result.getUserId()).isEqualTo("user-1");
    assertThat(result.getArticleId()).isEqualTo("article-1");

    mockServer.verify();
  }

  @Test
  void shouldGetCommentsByArticleIdViaHttpClient() throws Exception {
    String responseJson =
        "{\"comments\":[{\"id\":\"comment-1\",\"body\":\"First comment\",\"userId\":\"user-1\",\"articleId\":\"article-1\",\"createdAt\":\"2024-01-01T00:00:00.000Z\",\"updatedAt\":\"2024-01-01T00:00:00.000Z\"},{\"id\":\"comment-2\",\"body\":\"Second comment\",\"userId\":\"user-2\",\"articleId\":\"article-1\",\"createdAt\":\"2024-01-02T00:00:00.000Z\",\"updatedAt\":\"2024-01-02T00:00:00.000Z\"}]}";

    mockServer
        .expect(requestTo("http://localhost:8081/api/comments/articles/article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    List<CommentResponse> results = commentServiceClient.getCommentsByArticleId("article-1");

    assertThat(results).hasSize(2);
    assertThat(results.get(0).getId()).isEqualTo("comment-1");
    assertThat(results.get(0).getBody()).isEqualTo("First comment");
    assertThat(results.get(1).getId()).isEqualTo("comment-2");
    assertThat(results.get(1).getBody()).isEqualTo("Second comment");

    mockServer.verify();
  }

  @Test
  void shouldGetCommentByIdViaHttpClient() throws Exception {
    String responseJson =
        "{\"comment\":{\"id\":\"comment-1\",\"body\":\"A comment\",\"userId\":\"user-1\",\"articleId\":\"article-1\",\"createdAt\":\"2024-01-01T00:00:00.000Z\",\"updatedAt\":\"2024-01-01T00:00:00.000Z\"}}";

    mockServer
        .expect(requestTo("http://localhost:8081/api/comments/comment-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    Optional<CommentResponse> result = commentServiceClient.getCommentById("comment-1");

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo("comment-1");
    assertThat(result.get().getBody()).isEqualTo("A comment");

    mockServer.verify();
  }

  @Test
  void shouldGetCommentByArticleIdAndIdViaHttpClient() throws Exception {
    String responseJson =
        "{\"comment\":{\"id\":\"comment-1\",\"body\":\"A comment\",\"userId\":\"user-1\",\"articleId\":\"article-1\",\"createdAt\":\"2024-01-01T00:00:00.000Z\",\"updatedAt\":\"2024-01-01T00:00:00.000Z\"}}";

    mockServer
        .expect(requestTo("http://localhost:8081/api/comments/articles/article-1/comment-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    Optional<CommentResponse> result =
        commentServiceClient.getCommentByArticleIdAndId("article-1", "comment-1");

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo("comment-1");

    mockServer.verify();
  }

  @Test
  void shouldDeleteCommentViaHttpClient() throws Exception {
    mockServer
        .expect(requestTo("http://localhost:8081/api/comments/comment-1"))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withSuccess());

    commentServiceClient.deleteComment("comment-1");

    mockServer.verify();
  }
}
