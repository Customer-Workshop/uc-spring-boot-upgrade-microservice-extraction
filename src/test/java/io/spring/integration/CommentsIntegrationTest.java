package io.spring.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentDto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests that verify the monolith communicates correctly with the Comments microservice
 * via HTTP. These tests require the comments-service to be running on the configured URL.
 *
 * <p>Run with: ./gradlew test --tests "io.spring.integration.*"
 *
 * <p>Before running, start the comments-service: cd comments-service && ./gradlew bootRun
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("integration")
public class CommentsIntegrationTest {

  @Autowired private CommentServiceClient commentServiceClient;

  @Test
  public void should_create_comment_via_http() {
    String commentId = UUID.randomUUID().toString();
    String articleId = "integration-test-article-" + UUID.randomUUID();

    CommentDto created =
        commentServiceClient.createComment(commentId, "Integration test comment", "user-1", articleId);

    assertNotNull(created);
    assertEquals(commentId, created.getId());
    assertEquals("Integration test comment", created.getBody());
    assertEquals("user-1", created.getUserId());
    assertEquals(articleId, created.getArticleId());
    assertNotNull(created.getCreatedAt());
  }

  @Test
  public void should_find_comment_by_id_and_article_id() {
    String commentId = UUID.randomUUID().toString();
    String articleId = "integration-test-article-" + UUID.randomUUID();
    commentServiceClient.createComment(commentId, "Find me", "user-1", articleId);

    Optional<CommentDto> found = commentServiceClient.findById(commentId, articleId);

    assertTrue(found.isPresent());
    assertEquals("Find me", found.get().getBody());
  }

  @Test
  public void should_return_empty_for_nonexistent_comment() {
    Optional<CommentDto> found =
        commentServiceClient.findById("nonexistent-id", "nonexistent-article");

    assertFalse(found.isPresent());
  }

  @Test
  public void should_list_comments_by_article_id() {
    String articleId = "integration-list-article-" + UUID.randomUUID();
    commentServiceClient.createComment(
        UUID.randomUUID().toString(), "Comment 1", "user-1", articleId);
    commentServiceClient.createComment(
        UUID.randomUUID().toString(), "Comment 2", "user-2", articleId);

    List<CommentDto> comments = commentServiceClient.findByArticleId(articleId);

    assertEquals(2, comments.size());
  }

  @Test
  public void should_delete_comment() {
    String commentId = UUID.randomUUID().toString();
    String articleId = "integration-delete-article-" + UUID.randomUUID();
    commentServiceClient.createComment(commentId, "Delete me", "user-1", articleId);

    commentServiceClient.deleteComment(commentId);

    Optional<CommentDto> found = commentServiceClient.findById(commentId, articleId);
    assertFalse(found.isPresent());
  }

  @Test
  public void should_create_and_retrieve_multiple_comments_for_same_article() {
    String articleId = "integration-multi-article-" + UUID.randomUUID();
    String id1 = UUID.randomUUID().toString();
    String id2 = UUID.randomUUID().toString();
    String id3 = UUID.randomUUID().toString();

    commentServiceClient.createComment(id1, "First comment", "user-1", articleId);
    commentServiceClient.createComment(id2, "Second comment", "user-2", articleId);
    commentServiceClient.createComment(id3, "Third comment", "user-1", articleId);

    List<CommentDto> comments = commentServiceClient.findByArticleId(articleId);
    assertEquals(3, comments.size());

    Optional<CommentDto> second = commentServiceClient.findById(id2, articleId);
    assertTrue(second.isPresent());
    assertEquals("Second comment", second.get().getBody());
  }
}
