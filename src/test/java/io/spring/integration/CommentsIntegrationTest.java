package io.spring.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.core.comment.Comment;
import io.spring.infrastructure.service.CommentServiceClient;
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

  private MockRestServiceServer mockServer;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setUp() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
  }

  @Test
  public void should_create_comment_via_microservice() throws Exception {
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("id", "comment-remote-1");
    responseBody.put("body", "test comment");
    responseBody.put("userId", "user-1");
    responseBody.put("articleId", "article-1");
    responseBody.put("createdAt", "2026-01-01T00:00:00.000Z");
    responseBody.put("updatedAt", "2026-01-01T00:00:00.000Z");

    mockServer
        .expect(requestTo("http://localhost:8081/api/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withStatus(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(responseBody)));

    Comment comment = new Comment("test comment", "user-1", "article-1");
    String returnedId = commentServiceClient.saveAndReturnId(comment);

    assertNotNull(returnedId);
    assertEquals("comment-remote-1", returnedId);
    mockServer.verify();
  }

  @Test
  public void should_fetch_comments_by_article_from_microservice() throws Exception {
    Map<String, Object> comment1 = new HashMap<>();
    comment1.put("id", "c1");
    comment1.put("body", "first comment");
    comment1.put("userId", "user-1");
    comment1.put("articleId", "article-1");
    comment1.put("createdAt", "2026-01-01T00:00:00.000Z");
    comment1.put("updatedAt", "2026-01-01T00:00:00.000Z");

    Map<String, Object> comment2 = new HashMap<>();
    comment2.put("id", "c2");
    comment2.put("body", "second comment");
    comment2.put("userId", "user-2");
    comment2.put("articleId", "article-1");
    comment2.put("createdAt", "2026-01-02T00:00:00.000Z");
    comment2.put("updatedAt", "2026-01-02T00:00:00.000Z");

    mockServer
        .expect(requestTo("http://localhost:8081/api/comments?articleId=article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                objectMapper.writeValueAsString(List.of(comment1, comment2)),
                MediaType.APPLICATION_JSON));

    List<Comment> comments = commentServiceClient.findByArticleId("article-1");
    assertEquals(2, comments.size());
    assertEquals("c1", comments.get(0).getId());
    assertEquals("first comment", comments.get(0).getBody());
    assertEquals("c2", comments.get(1).getId());
    mockServer.verify();
  }

  @Test
  public void should_find_comment_by_id_from_microservice() throws Exception {
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("id", "c1");
    responseBody.put("body", "a comment");
    responseBody.put("userId", "user-1");
    responseBody.put("articleId", "article-1");
    responseBody.put("createdAt", "2026-01-01T00:00:00.000Z");
    responseBody.put("updatedAt", "2026-01-01T00:00:00.000Z");

    mockServer
        .expect(
            requestTo(
                "http://localhost:8081/api/comments/by-article-and-id?articleId=article-1&id=c1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(objectMapper.writeValueAsString(responseBody), MediaType.APPLICATION_JSON));

    Optional<Comment> result = commentServiceClient.findById("article-1", "c1");
    assertTrue(result.isPresent());
    assertEquals("c1", result.get().getId());
    assertEquals("a comment", result.get().getBody());
    mockServer.verify();
  }

  @Test
  public void should_return_empty_when_comment_not_found() throws Exception {
    mockServer
        .expect(
            requestTo(
                "http://localhost:8081/api/comments/by-article-and-id?articleId=article-1&id=missing"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    Optional<Comment> result = commentServiceClient.findById("article-1", "missing");
    assertFalse(result.isPresent());
    mockServer.verify();
  }

  @Test
  public void should_delete_comment_via_microservice() throws Exception {
    mockServer
        .expect(requestTo("http://localhost:8081/api/comments/c1"))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withNoContent());

    Comment comment =
        new Comment("c1", "body", "user-1", "article-1", org.joda.time.DateTime.now());
    commentServiceClient.remove(comment);
    mockServer.verify();
  }

  @Test
  public void should_find_comment_by_id_direct_from_microservice() throws Exception {
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("id", "c1");
    responseBody.put("body", "direct lookup");
    responseBody.put("userId", "user-1");
    responseBody.put("articleId", "article-1");
    responseBody.put("createdAt", "2026-01-01T00:00:00.000Z");

    mockServer
        .expect(requestTo("http://localhost:8081/api/comments/c1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(objectMapper.writeValueAsString(responseBody), MediaType.APPLICATION_JSON));

    Optional<Comment> result = commentServiceClient.findByIdDirect("c1");
    assertTrue(result.isPresent());
    assertEquals("direct lookup", result.get().getBody());
    mockServer.verify();
  }
}
