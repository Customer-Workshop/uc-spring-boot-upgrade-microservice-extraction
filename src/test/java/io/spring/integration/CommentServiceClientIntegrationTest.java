package io.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.spring.infrastructure.service.comments.CommentServiceClient;
import io.spring.infrastructure.service.comments.CommentServiceClient.CommentServiceResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests that verify communication between the monolith's HTTP client and the Comments
 * microservice. These tests require the Comments microservice to be running on port 8081.
 *
 * <p>Run with: ./gradlew test --tests "io.spring.integration.*"
 *
 * <p>Ensure the comments-service is running first: cd comments-service && ./gradlew bootRun
 */
public class CommentServiceClientIntegrationTest {

  private CommentServiceClient client;

  @BeforeEach
  void setUp() {
    String commentsServiceUrl = System.getProperty("comments.service.url", "http://localhost:8081");
    client = new CommentServiceClient(new RestTemplate(), commentsServiceUrl);
  }

  @Test
  void shouldCreateComment() {
    CommentServiceResponse response =
        client.createComment("test-id-1", "Integration test comment", "user-1", "article-1");

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo("test-id-1");
    assertThat(response.getBody()).isEqualTo("Integration test comment");
    assertThat(response.getUserId()).isEqualTo("user-1");
    assertThat(response.getArticleId()).isEqualTo("article-1");
    assertThat(response.getCreatedAt()).isNotNull();
  }

  @Test
  void shouldGetCommentsByArticleId() {
    client.createComment(null, "Comment for article-99", "user-1", "article-99");
    client.createComment(null, "Another comment for article-99", "user-2", "article-99");

    List<CommentServiceResponse> comments = client.getCommentsByArticleId("article-99");

    assertThat(comments).hasSizeGreaterThanOrEqualTo(2);
    assertThat(comments).allMatch(c -> c.getArticleId().equals("article-99"));
  }

  @Test
  void shouldGetCommentById() {
    CommentServiceResponse created =
        client.createComment("test-get-by-id", "Get by ID test", "user-1", "article-1");

    Optional<CommentServiceResponse> found = client.getCommentById(created.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getBody()).isEqualTo("Get by ID test");
  }

  @Test
  void shouldGetCommentByIdAndArticleId() {
    CommentServiceResponse created =
        client.createComment(
            "test-get-by-id-article", "Get by ID and article", "user-1", "article-50");

    Optional<CommentServiceResponse> found =
        client.getCommentByIdAndArticleId(created.getId(), "article-50");

    assertThat(found).isPresent();
    assertThat(found.get().getBody()).isEqualTo("Get by ID and article");
  }

  @Test
  void shouldReturnEmptyWhenCommentNotFound() {
    Optional<CommentServiceResponse> found = client.getCommentById("nonexistent-id");

    assertThat(found).isEmpty();
  }

  @Test
  void shouldDeleteComment() {
    CommentServiceResponse created =
        client.createComment("test-delete-id", "To be deleted", "user-1", "article-1");

    client.deleteComment(created.getId());

    Optional<CommentServiceResponse> found = client.getCommentById(created.getId());
    assertThat(found).isEmpty();
  }

  @Test
  void shouldReturnSeedDataComments() {
    List<CommentServiceResponse> comments = client.getCommentsByArticleId("article-1");

    assertThat(comments).hasSizeGreaterThanOrEqualTo(2);
    assertThat(comments.stream().anyMatch(c -> c.getId().equals("comment-1"))).isTrue();
    assertThat(comments.stream().anyMatch(c -> c.getId().equals("comment-2"))).isTrue();
  }
}
