package io.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class CommentsIntegrationTest {

  @Autowired private CommentServiceClient commentServiceClient;
  @Autowired private RestTemplate restTemplate;
  @Autowired private ObjectMapper objectMapper;

  private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
  }

  @Test
  void shouldCreateCommentViaHttpClient() throws Exception {
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("id", "comment-123");
    responseBody.put("body", "Test comment");
    responseBody.put("userId", "user-1");
    responseBody.put("articleId", "article-1");
    responseBody.put("createdAt", "2024-01-01T00:00:00Z");
    responseBody.put("updatedAt", "2024-01-01T00:00:00Z");

    mockServer
        .expect(requestTo("http://localhost:8081/api/articles/article-1/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(objectMapper.writeValueAsString(responseBody), MediaType.APPLICATION_JSON));

    CommentResponse result =
        commentServiceClient.createComment("article-1", "Test comment", "user-1");

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo("comment-123");
    assertThat(result.getBody()).isEqualTo("Test comment");
    assertThat(result.getUserId()).isEqualTo("user-1");
    assertThat(result.getArticleId()).isEqualTo("article-1");
    mockServer.verify();
  }

  @Test
  void shouldGetCommentsByArticleIdViaHttpClient() throws Exception {
    String responseBody =
        "[{\"id\":\"c1\",\"body\":\"Comment 1\",\"userId\":\"u1\","
            + "\"articleId\":\"article-1\",\"createdAt\":\"2024-01-01T00:00:00Z\","
            + "\"updatedAt\":\"2024-01-01T00:00:00Z\"},"
            + "{\"id\":\"c2\",\"body\":\"Comment 2\",\"userId\":\"u2\","
            + "\"articleId\":\"article-1\",\"createdAt\":\"2024-01-02T00:00:00Z\","
            + "\"updatedAt\":\"2024-01-02T00:00:00Z\"}]";

    mockServer
        .expect(requestTo("http://localhost:8081/api/articles/article-1/comments"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

    List<CommentResponse> comments = commentServiceClient.getCommentsByArticleId("article-1");

    assertThat(comments).hasSize(2);
    assertThat(comments.get(0).getId()).isEqualTo("c1");
    assertThat(comments.get(0).getBody()).isEqualTo("Comment 1");
    assertThat(comments.get(1).getId()).isEqualTo("c2");
    mockServer.verify();
  }

  @Test
  void shouldGetSingleCommentViaHttpClient() throws Exception {
    String responseBody =
        "{\"id\":\"c1\",\"body\":\"A comment\",\"userId\":\"u1\","
            + "\"articleId\":\"article-1\",\"createdAt\":\"2024-01-01T00:00:00Z\","
            + "\"updatedAt\":\"2024-01-01T00:00:00Z\"}";

    mockServer
        .expect(requestTo("http://localhost:8081/api/articles/article-1/comments/c1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

    Optional<CommentResponse> result = commentServiceClient.getComment("article-1", "c1");

    assertThat(result).isPresent();
    assertThat(result.get().getBody()).isEqualTo("A comment");
    mockServer.verify();
  }

  @Test
  void shouldDeleteCommentViaHttpClient() throws Exception {
    mockServer
        .expect(requestTo("http://localhost:8081/api/articles/article-1/comments/c1"))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withNoContent());

    commentServiceClient.deleteComment("article-1", "c1");

    mockServer.verify();
  }

  @Test
  void shouldReturnEmptyOptionalForNonExistentComment() throws Exception {
    mockServer
        .expect(requestTo("http://localhost:8081/api/articles/article-1/comments/nonexistent"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    Optional<CommentResponse> result = commentServiceClient.getComment("article-1", "nonexistent");

    assertThat(result).isEmpty();
  }
}
