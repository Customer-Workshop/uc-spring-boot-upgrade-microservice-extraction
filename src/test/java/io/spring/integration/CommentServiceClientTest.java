package io.spring.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.spring.infrastructure.service.CommentServiceClient;
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

public class CommentServiceClientTest {

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
  void should_create_comment_via_http() {
    String responseJson =
        "{\"comment\":{\"id\":\"comment-1\",\"body\":\"test body\","
            + "\"userId\":\"user-1\",\"articleId\":\"article-1\","
            + "\"createdAt\":1700000000000,\"updatedAt\":1700000000000}}";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    CommentResponse result = client.createComment("test body", "user-1", "article-1");

    assertEquals("comment-1", result.getId());
    assertEquals("test body", result.getBody());
    assertEquals("user-1", result.getUserId());
    assertEquals("article-1", result.getArticleId());
    mockServer.verify();
  }

  @Test
  void should_find_comment_by_id() {
    String responseJson =
        "{\"comment\":{\"id\":\"comment-2\",\"body\":\"found\","
            + "\"userId\":\"user-2\",\"articleId\":\"article-2\","
            + "\"createdAt\":1700000000000,\"updatedAt\":1700000000000}}";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/comment-2"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    Optional<CommentResponse> result = client.findById("comment-2");

    assertTrue(result.isPresent());
    assertEquals("comment-2", result.get().getId());
    assertEquals("found", result.get().getBody());
    mockServer.verify();
  }

  @Test
  void should_return_empty_when_comment_not_found() {
    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/nonexistent"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    Optional<CommentResponse> result = client.findById("nonexistent");

    assertFalse(result.isPresent());
    mockServer.verify();
  }

  @Test
  void should_find_comment_by_article_and_id() {
    String responseJson =
        "{\"comment\":{\"id\":\"comment-3\",\"body\":\"article comment\","
            + "\"userId\":\"user-3\",\"articleId\":\"article-3\","
            + "\"createdAt\":1700000000000,\"updatedAt\":1700000000000}}";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/article/article-3/comment-3"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    Optional<CommentResponse> result = client.findByArticleIdAndId("article-3", "comment-3");

    assertTrue(result.isPresent());
    assertEquals("comment-3", result.get().getId());
    mockServer.verify();
  }

  @Test
  void should_find_comments_by_article_id() {
    String responseJson =
        "{\"comments\":[{\"id\":\"c1\",\"body\":\"first\","
            + "\"userId\":\"u1\",\"articleId\":\"a1\","
            + "\"createdAt\":1700000000000,\"updatedAt\":1700000000000},"
            + "{\"id\":\"c2\",\"body\":\"second\","
            + "\"userId\":\"u2\",\"articleId\":\"a1\","
            + "\"createdAt\":1700000001000,\"updatedAt\":1700000001000}]}";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/article/a1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    List<CommentResponse> result = client.findByArticleId("a1");

    assertEquals(2, result.size());
    assertEquals("c1", result.get(0).getId());
    assertEquals("c2", result.get(1).getId());
    mockServer.verify();
  }

  @Test
  void should_delete_comment() {
    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/comment-del"))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withStatus(HttpStatus.NO_CONTENT));

    client.deleteComment("comment-del");

    mockServer.verify();
  }

  @Test
  void should_return_empty_list_when_no_comments_for_article() {
    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/article/no-comments"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{\"comments\":[]}", MediaType.APPLICATION_JSON));

    List<CommentResponse> result = client.findByArticleId("no-comments");

    assertTrue(result.isEmpty());
    mockServer.verify();
  }
}
