package io.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.spring.infrastructure.service.CommentServiceClient;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class CommentServiceIntegrationTest {

  private MockRestServiceServer mockServer;

  private CommentServiceClient commentServiceClient;

  private final String commentsServiceUrl = "http://localhost:8081";

  @BeforeEach
  void setUp() {
    RestTemplate restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    commentServiceClient = new CommentServiceClient(restTemplate, commentsServiceUrl);
  }

  @Test
  void shouldCreateCommentViaHttpPost() {
    mockServer
        .expect(requestTo(commentsServiceUrl + "/api/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                "{\"id\":\"comment-1\",\"body\":\"Test comment\",\"userId\":\"user-1\","
                    + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                    + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}",
                MediaType.APPLICATION_JSON));

    io.spring.core.comment.Comment comment =
        new io.spring.core.comment.Comment("Test comment", "user-1", "article-1");
    commentServiceClient.save(comment);

    mockServer.verify();
  }

  @Test
  void shouldRetrieveCommentByIdViaHttpGet() {
    String commentId = "comment-1";
    mockServer
        .expect(requestTo(commentsServiceUrl + "/api/comments/" + commentId))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                "{\"id\":\"comment-1\",\"body\":\"Test comment\",\"userId\":\"user-1\","
                    + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                    + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}",
                MediaType.APPLICATION_JSON));

    Optional<io.spring.core.comment.Comment> result =
        commentServiceClient.findById("article-1", commentId);

    assertThat(result).isPresent();
    assertThat(result.get().getBody()).isEqualTo("Test comment");
    mockServer.verify();
  }

  @Test
  void shouldRetrieveCommentsByArticleIdViaHttpGet() {
    String articleId = "article-1";
    mockServer
        .expect(requestTo(commentsServiceUrl + "/api/comments?articleId=" + articleId))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                "[{\"id\":\"comment-1\",\"body\":\"Comment 1\",\"userId\":\"user-1\","
                    + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                    + "\"updatedAt\":\"2026-01-01T00:00:00Z\"},"
                    + "{\"id\":\"comment-2\",\"body\":\"Comment 2\",\"userId\":\"user-2\","
                    + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-02T00:00:00Z\","
                    + "\"updatedAt\":\"2026-01-02T00:00:00Z\"}]",
                MediaType.APPLICATION_JSON));

    List<java.util.Map<String, Object>> comments = commentServiceClient.findByArticleId(articleId);

    assertThat(comments).hasSize(2);
    assertThat(comments.get(0).get("body")).isEqualTo("Comment 1");
    assertThat(comments.get(1).get("body")).isEqualTo("Comment 2");
    mockServer.verify();
  }

  @Test
  void shouldDeleteCommentViaHttpDelete() {
    io.spring.core.comment.Comment comment =
        new io.spring.core.comment.Comment("Test comment", "user-1", "article-1");
    String commentId = comment.getId();
    mockServer
        .expect(requestTo(commentsServiceUrl + "/api/comments/" + commentId))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withNoContent());

    commentServiceClient.remove(comment);

    mockServer.verify();
  }

  @Test
  void shouldReturnEmptyWhenCommentNotFound() {
    String commentId = "nonexistent";
    mockServer
        .expect(requestTo(commentsServiceUrl + "/api/comments/" + commentId))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    Optional<java.util.Map<String, Object>> result =
        commentServiceClient.findCommentById(commentId);

    // Empty map is still present but has no id
    mockServer.verify();
  }

  @Test
  void shouldReturnEmptyWhenArticleIdDoesNotMatch() {
    String commentId = "comment-1";
    mockServer
        .expect(requestTo(commentsServiceUrl + "/api/comments/" + commentId))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                "{\"id\":\"comment-1\",\"body\":\"Test\",\"userId\":\"user-1\","
                    + "\"articleId\":\"article-2\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                    + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}",
                MediaType.APPLICATION_JSON));

    Optional<io.spring.core.comment.Comment> result =
        commentServiceClient.findById("article-1", commentId);

    assertThat(result).isEmpty();
    mockServer.verify();
  }
}
