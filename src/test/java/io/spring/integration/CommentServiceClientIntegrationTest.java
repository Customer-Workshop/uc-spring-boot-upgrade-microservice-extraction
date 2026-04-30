package io.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.spring.core.comment.Comment;
import io.spring.infrastructure.service.CommentServiceClient;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests that verify the monolith's CommentServiceClient communicates correctly with the
 * Comments microservice. These tests require the comments-service to be running on port 8081.
 *
 * <p>Run with: ./gradlew test --tests "io.spring.integration.*"
 *
 * <p>Ensure the comments-service is running first: cd comments-service && ./gradlew bootRun
 */
public class CommentServiceClientIntegrationTest {

  private CommentServiceClient client;
  private static final String COMMENTS_SERVICE_URL = "http://localhost:8081";

  @BeforeEach
  void setUp() {
    RestTemplate restTemplate = new RestTemplate();
    client = new CommentServiceClient(restTemplate, COMMENTS_SERVICE_URL);
  }

  @Test
  void should_create_comment_via_microservice() {
    Comment comment = client.createComment("Test comment body", "user-123", "article-456");

    assertThat(comment).isNotNull();
    assertThat(comment.getId()).isNotBlank();
    assertThat(comment.getBody()).isEqualTo("Test comment body");
    assertThat(comment.getUserId()).isEqualTo("user-123");
    assertThat(comment.getArticleId()).isEqualTo("article-456");
    assertThat(comment.getCreatedAt()).isNotNull();
  }

  @Test
  void should_get_comments_by_article_id() {
    String articleId = "article-integration-" + System.currentTimeMillis();
    client.createComment("Comment 1", "user-1", articleId);
    client.createComment("Comment 2", "user-2", articleId);

    List<Comment> comments = client.getCommentsByArticleId(articleId);

    assertThat(comments).hasSize(2);
    assertThat(comments).extracting(Comment::getArticleId).containsOnly(articleId);
  }

  @Test
  void should_get_comment_by_id_and_article_id() {
    String articleId = "article-lookup-" + System.currentTimeMillis();
    Comment created = client.createComment("Lookup test", "user-1", articleId);

    Optional<Comment> found = client.getCommentByIdAndArticleId(articleId, created.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(created.getId());
    assertThat(found.get().getBody()).isEqualTo("Lookup test");
  }

  @Test
  void should_return_empty_for_nonexistent_comment() {
    Optional<Comment> found =
        client.getCommentByIdAndArticleId("nonexistent-article", "nonexistent-id");

    assertThat(found).isEmpty();
  }

  @Test
  void should_delete_comment_via_microservice() {
    String articleId = "article-delete-" + System.currentTimeMillis();
    Comment created = client.createComment("To be deleted", "user-1", articleId);

    client.deleteComment(created.getId());

    Optional<Comment> found = client.getCommentByIdAndArticleId(articleId, created.getId());
    assertThat(found).isEmpty();
  }

  @Test
  void should_create_and_retrieve_multiple_comments_for_same_article() {
    String articleId = "article-multi-" + System.currentTimeMillis();

    client.createComment("First comment", "user-a", articleId);
    client.createComment("Second comment", "user-b", articleId);
    client.createComment("Third comment", "user-c", articleId);

    List<Comment> comments = client.getCommentsByArticleId(articleId);

    assertThat(comments).hasSize(3);
    assertThat(comments)
        .extracting(Comment::getBody)
        .containsExactlyInAnyOrder("First comment", "Second comment", "Third comment");
  }

  @Test
  void should_only_return_comments_for_specified_article() {
    String articleId1 = "article-iso-1-" + System.currentTimeMillis();
    String articleId2 = "article-iso-2-" + System.currentTimeMillis();

    client.createComment("Article 1 comment", "user-1", articleId1);
    client.createComment("Article 2 comment", "user-1", articleId2);

    List<Comment> article1Comments = client.getCommentsByArticleId(articleId1);
    List<Comment> article2Comments = client.getCommentsByArticleId(articleId2);

    assertThat(article1Comments).hasSize(1);
    assertThat(article1Comments.get(0).getBody()).isEqualTo("Article 1 comment");
    assertThat(article2Comments).hasSize(1);
    assertThat(article2Comments.get(0).getBody()).isEqualTo("Article 2 comment");
  }
}
