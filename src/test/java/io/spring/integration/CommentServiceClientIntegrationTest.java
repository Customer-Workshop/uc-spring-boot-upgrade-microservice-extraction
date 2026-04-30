package io.spring.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.spring.core.comment.Comment;
import io.spring.infrastructure.service.CommentServiceClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class CommentServiceClientIntegrationTest {

  private MockRestServiceServer mockServer;
  private CommentServiceClient commentServiceClient;
  private RestTemplate restTemplate;

  @BeforeEach
  void setUp() {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    commentServiceClient = new CommentServiceClient(restTemplate, "http://comments-service:8081");
  }

  @Test
  void shouldSaveComment() {
    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withSuccess(
                "{\"id\":\"comment-1\",\"body\":\"Hello\",\"userId\":\"user-1\",\"articleId\":\"article-1\","
                    + "\"createdAt\":\"2024-01-01T00:00:00Z\",\"updatedAt\":\"2024-01-01T00:00:00Z\"}",
                MediaType.APPLICATION_JSON));

    Comment comment = new Comment("Hello", "user-1", "article-1");
    commentServiceClient.save(comment);

    mockServer.verify();
  }

  @Test
  void shouldFindCommentById() {
    mockServer
        .expect(
            requestTo("http://comments-service:8081/api/comments/comment-1?articleId=article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                "{\"id\":\"comment-1\",\"body\":\"Hello\",\"userId\":\"user-1\",\"articleId\":\"article-1\","
                    + "\"createdAt\":\"2024-01-01T00:00:00Z\",\"updatedAt\":\"2024-01-01T00:00:00Z\"}",
                MediaType.APPLICATION_JSON));

    Optional<Comment> comment = commentServiceClient.findById("article-1", "comment-1");

    assertTrue(comment.isPresent());
    assertEquals("Hello", comment.get().getBody());
    assertEquals("user-1", comment.get().getUserId());
    mockServer.verify();
  }

  @Test
  void shouldReturnEmptyForNonExistentComment() {
    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/no-exist?articleId=article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    Optional<Comment> comment = commentServiceClient.findById("article-1", "no-exist");

    assertFalse(comment.isPresent());
    mockServer.verify();
  }

  @Test
  void shouldRemoveComment() {
    Comment comment = new Comment("Hello", "user-1", "article-1");

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/" + comment.getId()))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withNoContent());

    commentServiceClient.remove(comment);

    mockServer.verify();
  }

  @Test
  void shouldFindCommentsByArticleId() {
    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments?articleId=article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                "[{\"id\":\"c1\",\"body\":\"First\",\"userId\":\"u1\",\"articleId\":\"article-1\","
                    + "\"createdAt\":\"2024-01-01T00:00:00Z\",\"updatedAt\":\"2024-01-01T00:00:00Z\"},"
                    + "{\"id\":\"c2\",\"body\":\"Second\",\"userId\":\"u2\",\"articleId\":\"article-1\","
                    + "\"createdAt\":\"2024-01-02T00:00:00Z\",\"updatedAt\":\"2024-01-02T00:00:00Z\"}]",
                MediaType.APPLICATION_JSON));

    List<Map<String, Object>> comments = commentServiceClient.findByArticleId("article-1");

    assertEquals(2, comments.size());
    assertEquals("First", comments.get(0).get("body"));
    assertEquals("Second", comments.get(1).get("body"));
    mockServer.verify();
  }

  @Test
  void shouldFindRawById() {
    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/comment-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                "{\"id\":\"comment-1\",\"body\":\"Hello\",\"userId\":\"user-1\",\"articleId\":\"article-1\","
                    + "\"createdAt\":\"2024-01-01T00:00:00Z\",\"updatedAt\":\"2024-01-01T00:00:00Z\"}",
                MediaType.APPLICATION_JSON));

    Optional<Map<String, Object>> raw = commentServiceClient.findRawById("comment-1");

    assertTrue(raw.isPresent());
    assertEquals("Hello", raw.get().get("body"));
    mockServer.verify();
  }
}
