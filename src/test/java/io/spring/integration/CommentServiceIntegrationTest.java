package io.spring.integration;

import io.spring.core.comment.Comment;
import io.spring.infrastructure.service.client.CommentServiceClient;
import io.spring.infrastructure.service.client.CommentServiceResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

public class CommentServiceIntegrationTest {

  private RestTemplate restTemplate;
  private MockRestServiceServer mockServer;
  private CommentServiceClient commentServiceClient;

  @BeforeEach
  public void setUp() {
    restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    commentServiceClient = new CommentServiceClient(restTemplate, "http://comments-service:8081");
  }

  @Test
  public void should_create_comment_via_service_and_return_id() {
    String responseJson =
        "{\"comment\": {\"id\": \"new-comment-id\", \"body\": \"test body\","
            + " \"articleId\": \"article-1\", \"userId\": \"user-1\","
            + " \"createdAt\": \"2026-06-30T10:00:00.000Z\","
            + " \"updatedAt\": \"2026-06-30T10:00:00.000Z\"}}";

    mockServer
        .expect(MockRestRequestMatchers.requestTo("http://comments-service:8081/api/comments"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
        .andRespond(
            MockRestResponseCreators.withStatus(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseJson));

    String id = commentServiceClient.saveAndReturnId("test body", "user-1", "article-1");
    Assertions.assertEquals("new-comment-id", id);
    mockServer.verify();
  }

  @Test
  public void should_retrieve_comments_by_article_id() {
    String responseJson =
        "{\"comments\": ["
            + "{\"id\": \"c1\", \"body\": \"comment 1\", \"articleId\": \"article-1\","
            + " \"userId\": \"user-1\", \"createdAt\": \"2026-06-30T10:00:00.000Z\","
            + " \"updatedAt\": \"2026-06-30T10:00:00.000Z\"},"
            + "{\"id\": \"c2\", \"body\": \"comment 2\", \"articleId\": \"article-1\","
            + " \"userId\": \"user-2\", \"createdAt\": \"2026-06-30T11:00:00.000Z\","
            + " \"updatedAt\": \"2026-06-30T11:00:00.000Z\"}"
            + "]}";

    mockServer
        .expect(
            MockRestRequestMatchers.requestTo(
                "http://comments-service:8081/api/comments?articleId=article-1"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andRespond(
            MockRestResponseCreators.withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseJson));

    List<CommentServiceResponse> responses =
        commentServiceClient.findCommentsByArticleId("article-1");
    Assertions.assertEquals(2, responses.size());
    Assertions.assertEquals("c1", responses.get(0).getId());
    Assertions.assertEquals("comment 1", responses.get(0).getBody());
    Assertions.assertEquals("c2", responses.get(1).getId());
    Assertions.assertEquals("comment 2", responses.get(1).getBody());
    mockServer.verify();
  }

  @Test
  public void should_find_comment_by_id_with_article_id() {
    String responseJson =
        "{\"comment\": {\"id\": \"c1\", \"body\": \"test comment\","
            + " \"articleId\": \"article-1\", \"userId\": \"user-1\","
            + " \"createdAt\": \"2026-06-30T10:00:00.000Z\","
            + " \"updatedAt\": \"2026-06-30T10:00:00.000Z\"}}";

    mockServer
        .expect(
            MockRestRequestMatchers.requestTo(
                "http://comments-service:8081/api/comments/c1?articleId=article-1"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andRespond(
            MockRestResponseCreators.withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseJson));

    Optional<Comment> comment = commentServiceClient.findById("article-1", "c1");
    Assertions.assertTrue(comment.isPresent());
    Assertions.assertEquals("test comment", comment.get().getBody());
    mockServer.verify();
  }

  @Test
  public void should_find_comment_by_id_only() {
    String responseJson =
        "{\"comment\": {\"id\": \"c1\", \"body\": \"test comment\","
            + " \"articleId\": \"article-1\", \"userId\": \"user-1\","
            + " \"createdAt\": \"2026-06-30T10:00:00.000Z\","
            + " \"updatedAt\": \"2026-06-30T10:00:00.000Z\"}}";

    mockServer
        .expect(
            MockRestRequestMatchers.requestTo(
                "http://comments-service:8081/api/comments/c1"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andRespond(
            MockRestResponseCreators.withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseJson));

    Optional<CommentServiceResponse> response = commentServiceClient.findCommentById("c1");
    Assertions.assertTrue(response.isPresent());
    Assertions.assertEquals("test comment", response.get().getBody());
    Assertions.assertEquals("user-1", response.get().getUserId());
    mockServer.verify();
  }

  @Test
  public void should_delete_comment_via_service() {
    Comment comment = new Comment("body", "user-1", "article-1");

    mockServer
        .expect(
            MockRestRequestMatchers.requestTo(
                "http://comments-service:8081/api/comments/" + comment.getId()))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
        .andRespond(MockRestResponseCreators.withNoContent());

    commentServiceClient.remove(comment);
    mockServer.verify();
  }

  @Test
  public void should_return_empty_when_comment_not_found() {
    mockServer
        .expect(
            MockRestRequestMatchers.requestTo(
                "http://comments-service:8081/api/comments/nonexistent?articleId=article-1"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND));

    Optional<Comment> result = commentServiceClient.findById("article-1", "nonexistent");
    Assertions.assertTrue(result.isEmpty());
    mockServer.verify();
  }

  @Test
  public void should_return_empty_list_on_service_error() {
    mockServer
        .expect(
            MockRestRequestMatchers.requestTo(
                "http://comments-service:8081/api/comments?articleId=article-1"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

    List<CommentServiceResponse> responses =
        commentServiceClient.findCommentsByArticleId("article-1");
    Assertions.assertTrue(responses.isEmpty());
    mockServer.verify();
  }
}
