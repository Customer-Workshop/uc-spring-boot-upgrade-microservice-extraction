package io.spring.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.core.comment.Comment;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentDto;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests that verify the monolith communicates correctly with the comments microservice.
 * These tests require the comments-service to be running on port 8081.
 *
 * <p>Run with: docker-compose up -d comments-service Then run these tests: ./gradlew test
 * --tests="io.spring.integration.*" -Dorg.gradle.java.home=$JAVA_HOME
 */
class CommentServiceClientIntegrationTest {

  private static final String COMMENTS_SERVICE_URL = "http://localhost:8081";
  private CommentServiceClient client;
  private boolean serviceAvailable;

  @BeforeEach
  void setUp() {
    client = new CommentServiceClient(COMMENTS_SERVICE_URL);
    serviceAvailable = isServiceAvailable();
  }

  @Test
  void shouldCreateAndRetrieveComment() {
    if (!serviceAvailable) {
      System.out.println("SKIPPED: Comments service not available at " + COMMENTS_SERVICE_URL);
      return;
    }

    Comment comment = new Comment("Integration test comment", "user-1", "article-integration-1");
    client.save(comment);

    Optional<Comment> found = client.findById("article-integration-1", comment.getId());
    assertTrue(found.isPresent(), "Comment should be found after creation");
    assertEquals("user-1", found.get().getUserId());
  }

  @Test
  void shouldListCommentsByArticleId() {
    if (!serviceAvailable) {
      System.out.println("SKIPPED: Comments service not available at " + COMMENTS_SERVICE_URL);
      return;
    }

    String articleId = "article-integration-list-" + System.currentTimeMillis();
    Comment comment1 = new Comment("First comment", "user-1", articleId);
    Comment comment2 = new Comment("Second comment", "user-2", articleId);
    client.save(comment1);
    client.save(comment2);

    List<CommentDto> comments = client.getCommentsByArticleId(articleId);
    assertEquals(2, comments.size(), "Should find 2 comments for the article");
  }

  @Test
  void shouldDeleteComment() {
    if (!serviceAvailable) {
      System.out.println("SKIPPED: Comments service not available at " + COMMENTS_SERVICE_URL);
      return;
    }

    String articleId = "article-integration-delete-" + System.currentTimeMillis();
    Comment comment = new Comment("Delete me", "user-1", articleId);
    client.save(comment);

    Optional<Comment> found = client.findById(articleId, comment.getId());
    assertTrue(found.isPresent(), "Comment should exist before deletion");

    client.remove(comment);

    Optional<Comment> afterDelete = client.findById(articleId, comment.getId());
    assertFalse(afterDelete.isPresent(), "Comment should not exist after deletion");
  }

  @Test
  void shouldReturnEmptyForNonExistentComment() {
    if (!serviceAvailable) {
      System.out.println("SKIPPED: Comments service not available at " + COMMENTS_SERVICE_URL);
      return;
    }

    Optional<Comment> found = client.findById("nonexistent-article", "nonexistent-id");
    assertFalse(found.isPresent(), "Should return empty for non-existent comment");
  }

  @Test
  void shouldReturnEmptyListForArticleWithNoComments() {
    if (!serviceAvailable) {
      System.out.println("SKIPPED: Comments service not available at " + COMMENTS_SERVICE_URL);
      return;
    }

    String articleId = "article-no-comments-" + System.currentTimeMillis();
    List<CommentDto> comments = client.getCommentsByArticleId(articleId);
    assertTrue(comments.isEmpty(), "Should return empty list for article with no comments");
  }

  @Test
  void shouldGetCommentById() {
    if (!serviceAvailable) {
      System.out.println("SKIPPED: Comments service not available at " + COMMENTS_SERVICE_URL);
      return;
    }

    String articleId = "article-get-by-id-" + System.currentTimeMillis();
    Comment comment = new Comment("Get by ID test", "user-1", articleId);
    client.save(comment);

    Optional<CommentDto> found = client.getCommentById(comment.getId());
    assertTrue(found.isPresent(), "Comment should be found by ID");
    assertEquals("Get by ID test", found.get().getBody());
    assertEquals("user-1", found.get().getUserId());
  }

  private boolean isServiceAvailable() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> response =
          restTemplate.getForEntity(
              COMMENTS_SERVICE_URL + "/api/comments?articleId=ping", String.class);
      return response.getStatusCode() == HttpStatus.OK;
    } catch (Exception e) {
      return false;
    }
  }
}
