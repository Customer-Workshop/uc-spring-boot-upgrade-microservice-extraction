package io.spring.integration;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.application.data.CommentData;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.service.comments.CommentServiceClient;
import io.spring.infrastructure.service.comments.CommentServiceClient.CommentResponseDto;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ActiveProfiles("test")
public class CommentsIntegrationTest {

  @Autowired private RestTemplate restTemplate;
  @Autowired private CommentRepository commentRepository;
  @Autowired private CommentServiceClient commentServiceClient;
  @Autowired private ObjectMapper objectMapper;

  @Value("${comments.service.url}")
  private String commentsServiceUrl;

  private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
  }

  @Test
  void shouldCreateCommentViaHttpClient() throws Exception {
    CommentResponseDto expectedResponse = new CommentResponseDto();
    expectedResponse.setId("comment-123");
    expectedResponse.setBody("Test comment body");
    expectedResponse.setUserId("user-1");
    expectedResponse.setArticleId("article-1");
    expectedResponse.setCreatedAt(Instant.now());
    expectedResponse.setUpdatedAt(Instant.now());

    mockServer
        .expect(ExpectedCount.once(), requestTo(commentsServiceUrl + "/api/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withStatus(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(expectedResponse)));

    CommentResponseDto result =
        commentServiceClient.createComment("comment-123", "Test comment body", "user-1", "article-1");

    mockServer.verify();
    assertEquals("comment-123", result.getId());
    assertEquals("Test comment body", result.getBody());
    assertEquals("user-1", result.getUserId());
    assertEquals("article-1", result.getArticleId());
  }

  @Test
  void shouldGetCommentsByArticleIdViaHttpClient() throws Exception {
    CommentResponseDto comment1 = new CommentResponseDto();
    comment1.setId("comment-1");
    comment1.setBody("First comment");
    comment1.setUserId("user-1");
    comment1.setArticleId("article-1");
    comment1.setCreatedAt(Instant.now());
    comment1.setUpdatedAt(Instant.now());

    CommentResponseDto comment2 = new CommentResponseDto();
    comment2.setId("comment-2");
    comment2.setBody("Second comment");
    comment2.setUserId("user-2");
    comment2.setArticleId("article-1");
    comment2.setCreatedAt(Instant.now());
    comment2.setUpdatedAt(Instant.now());

    CommentResponseDto[] commentsArray = {comment1, comment2};

    mockServer
        .expect(
            ExpectedCount.once(),
            requestTo(commentsServiceUrl + "/api/comments?articleId=article-1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                objectMapper.writeValueAsString(commentsArray), MediaType.APPLICATION_JSON));

    List<CommentResponseDto> result = commentServiceClient.getCommentsByArticleId("article-1");

    mockServer.verify();
    assertEquals(2, result.size());
    assertEquals("comment-1", result.get(0).getId());
    assertEquals("comment-2", result.get(1).getId());
  }

  @Test
  void shouldGetCommentByIdViaHttpClient() throws Exception {
    CommentResponseDto expectedComment = new CommentResponseDto();
    expectedComment.setId("comment-123");
    expectedComment.setBody("Test comment");
    expectedComment.setUserId("user-1");
    expectedComment.setArticleId("article-1");
    expectedComment.setCreatedAt(Instant.now());
    expectedComment.setUpdatedAt(Instant.now());

    mockServer
        .expect(
            ExpectedCount.once(), requestTo(commentsServiceUrl + "/api/comments/comment-123"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                objectMapper.writeValueAsString(expectedComment), MediaType.APPLICATION_JSON));

    Optional<CommentResponseDto> result = commentServiceClient.getCommentById("comment-123");

    mockServer.verify();
    assertTrue(result.isPresent());
    assertEquals("comment-123", result.get().getId());
    assertEquals("Test comment", result.get().getBody());
  }

  @Test
  void shouldReturnEmptyWhenCommentNotFound() throws Exception {
    mockServer
        .expect(
            ExpectedCount.once(), requestTo(commentsServiceUrl + "/api/comments/non-existent"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    Optional<CommentResponseDto> result = commentServiceClient.getCommentById("non-existent");

    mockServer.verify();
    assertFalse(result.isPresent());
  }

  @Test
  void shouldDeleteCommentViaHttpClient() throws Exception {
    mockServer
        .expect(
            ExpectedCount.once(), requestTo(commentsServiceUrl + "/api/comments/comment-123"))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withNoContent());

    commentServiceClient.deleteComment("comment-123");

    mockServer.verify();
  }

  @Test
  void shouldSaveCommentThroughRepository() throws Exception {
    Comment comment = new Comment("Test body", "user-1", "article-1");

    CommentResponseDto expectedResponse = new CommentResponseDto();
    expectedResponse.setId(comment.getId());
    expectedResponse.setBody(comment.getBody());
    expectedResponse.setUserId(comment.getUserId());
    expectedResponse.setArticleId(comment.getArticleId());
    expectedResponse.setCreatedAt(Instant.now());
    expectedResponse.setUpdatedAt(Instant.now());

    mockServer
        .expect(ExpectedCount.once(), requestTo(commentsServiceUrl + "/api/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(
            withStatus(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(expectedResponse)));

    commentRepository.save(comment);

    mockServer.verify();
  }

  @Test
  void shouldFindCommentByIdThroughRepository() throws Exception {
    CommentResponseDto expectedComment = new CommentResponseDto();
    expectedComment.setId("comment-456");
    expectedComment.setBody("Found comment");
    expectedComment.setUserId("user-2");
    expectedComment.setArticleId("article-2");
    expectedComment.setCreatedAt(Instant.now());
    expectedComment.setUpdatedAt(Instant.now());

    mockServer
        .expect(
            ExpectedCount.once(),
            requestTo(commentsServiceUrl + "/api/comments/by-article/article-2/comment-456"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(
            withSuccess(
                objectMapper.writeValueAsString(expectedComment), MediaType.APPLICATION_JSON));

    Optional<Comment> result = commentRepository.findById("article-2", "comment-456");

    mockServer.verify();
    assertTrue(result.isPresent());
    assertEquals("comment-456", result.get().getId());
    assertEquals("Found comment", result.get().getBody());
  }

  @Test
  void shouldDeleteCommentThroughRepository() throws Exception {
    Comment comment = new Comment("Test body", "user-1", "article-1");

    mockServer
        .expect(
            ExpectedCount.once(),
            requestTo(commentsServiceUrl + "/api/comments/" + comment.getId()))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withNoContent());

    commentRepository.remove(comment);

    mockServer.verify();
  }
}
