package io.spring.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.spring.core.comment.Comment;
import io.spring.infrastructure.repository.HttpCommentRepository;
import io.spring.infrastructure.service.CommentServiceClient;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class HttpCommentRepositoryTest {

  private HttpCommentRepository repository;
  private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    RestTemplate restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    CommentServiceClient client = new CommentServiceClient(restTemplate, "http://comments-service:8081");
    repository = new HttpCommentRepository(client);
  }

  @Test
  void should_save_comment_via_http() {
    String responseJson =
        "{\"comment\":{\"id\":\"new-id\",\"body\":\"hello\","
            + "\"userId\":\"u1\",\"articleId\":\"a1\","
            + "\"createdAt\":1700000000000,\"updatedAt\":1700000000000}}";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    Comment comment = new Comment("hello", "u1", "a1");
    repository.save(comment);

    mockServer.verify();
  }

  @Test
  void should_find_comment_by_article_and_id() {
    String responseJson =
        "{\"comment\":{\"id\":\"c1\",\"body\":\"test\","
            + "\"userId\":\"u1\",\"articleId\":\"a1\","
            + "\"createdAt\":1700000000000,\"updatedAt\":1700000000000}}";

    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/article/a1/c1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    Optional<Comment> result = repository.findById("a1", "c1");

    assertTrue(result.isPresent());
    assertEquals("c1", result.get().getId());
    assertEquals("test", result.get().getBody());
    assertEquals("u1", result.get().getUserId());
    assertEquals("a1", result.get().getArticleId());
    mockServer.verify();
  }

  @Test
  void should_return_empty_when_not_found() {
    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/article/a1/missing"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    Optional<Comment> result = repository.findById("a1", "missing");

    assertFalse(result.isPresent());
    mockServer.verify();
  }

  @Test
  void should_remove_comment_via_http() {
    mockServer
        .expect(requestTo("http://comments-service:8081/api/comments/c1"))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withStatus(HttpStatus.NO_CONTENT));

    Comment comment = new Comment();
    comment.setId("c1");
    repository.remove(comment);

    mockServer.verify();
  }
}
