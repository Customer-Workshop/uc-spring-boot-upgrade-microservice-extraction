package io.spring.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

public class CommentServiceClientTest {

  private RestTemplate restTemplate;
  private MockRestServiceServer mockServer;
  private CommentServiceClient client;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    client = new CommentServiceClient(restTemplate, "http://comments-service:8081");
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
  }

  @Test
  public void should_create_comment_via_http() throws Exception {
    Map<String, Object> commentBody = new HashMap<>();
    commentBody.put("id", "comment-123");
    commentBody.put("body", "Test comment");
    commentBody.put("articleId", "article-1");
    commentBody.put("userId", "user-1");

    Map<String, Object> response = new HashMap<>();
    response.put("comment", commentBody);

    mockServer
        .expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo(
                "http://comments-service:8081/api/articles/article-1/comments"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
        .andRespond(
            MockRestResponseCreators.withStatus(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(response)));

    CommentResponse result = client.createComment("article-1", "Test comment", "user-1");

    assertNotNull(result);
    assertEquals("comment-123", result.getId());
    assertEquals("Test comment", result.getBody());
    assertEquals("article-1", result.getArticleId());
    assertEquals("user-1", result.getUserId());

    mockServer.verify();
  }

  @Test
  public void should_get_comments_by_article_id() throws Exception {
    Map<String, Object> comment1 = new HashMap<>();
    comment1.put("id", "c1");
    comment1.put("body", "First");
    comment1.put("articleId", "article-1");
    comment1.put("userId", "user-1");

    Map<String, Object> comment2 = new HashMap<>();
    comment2.put("id", "c2");
    comment2.put("body", "Second");
    comment2.put("articleId", "article-1");
    comment2.put("userId", "user-2");

    Map<String, Object> response = new HashMap<>();
    response.put("comments", List.of(comment1, comment2));

    mockServer
        .expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo(
                "http://comments-service:8081/api/articles/article-1/comments"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andRespond(
            MockRestResponseCreators.withSuccess(
                objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

    List<CommentResponse> result = client.getCommentsByArticleId("article-1");

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("First", result.get(0).getBody());
    assertEquals("Second", result.get(1).getBody());

    mockServer.verify();
  }

  @Test
  public void should_get_single_comment() throws Exception {
    Map<String, Object> commentBody = new HashMap<>();
    commentBody.put("id", "c1");
    commentBody.put("body", "A comment");
    commentBody.put("articleId", "article-1");
    commentBody.put("userId", "user-1");

    Map<String, Object> response = new HashMap<>();
    response.put("comment", commentBody);

    mockServer
        .expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo(
                "http://comments-service:8081/api/articles/article-1/comments/c1"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andRespond(
            MockRestResponseCreators.withSuccess(
                objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

    Optional<CommentResponse> result = client.getComment("article-1", "c1");

    assertTrue(result.isPresent());
    assertEquals("A comment", result.get().getBody());

    mockServer.verify();
  }

  @Test
  public void should_return_empty_for_nonexistent_comment() throws Exception {
    mockServer
        .expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo(
                "http://comments-service:8081/api/articles/article-1/comments/nonexistent"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND));

    Optional<CommentResponse> result = client.getComment("article-1", "nonexistent");

    assertFalse(result.isPresent());

    mockServer.verify();
  }

  @Test
  public void should_get_comment_by_id() throws Exception {
    Map<String, Object> commentBody = new HashMap<>();
    commentBody.put("id", "c1");
    commentBody.put("body", "Fetched by ID");
    commentBody.put("articleId", "article-1");
    commentBody.put("userId", "user-1");

    Map<String, Object> response = new HashMap<>();
    response.put("comment", commentBody);

    mockServer
        .expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo("http://comments-service:8081/api/comments/c1"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andRespond(
            MockRestResponseCreators.withSuccess(
                objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

    Optional<CommentResponse> result = client.getCommentById("c1");

    assertTrue(result.isPresent());
    assertEquals("Fetched by ID", result.get().getBody());

    mockServer.verify();
  }

  @Test
  public void should_delete_comment() throws Exception {
    mockServer
        .expect(
            ExpectedCount.once(),
            MockRestRequestMatchers.requestTo(
                "http://comments-service:8081/api/articles/article-1/comments/c1"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
        .andRespond(MockRestResponseCreators.withNoContent());

    client.deleteComment("article-1", "c1");

    mockServer.verify();
  }
}
