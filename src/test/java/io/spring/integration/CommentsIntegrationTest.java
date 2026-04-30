package io.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests that verify the monolith communicates correctly with the Comments microservice
 * via HTTP. These tests require the comments-service to be running on the configured URL.
 *
 * <p>Run with: docker-compose up comments-service, then run these tests against the monolith with
 * -Dcomments.service.url=http://localhost:8081
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentsIntegrationTest {

  @Autowired private CommentServiceClient commentServiceClient;

  private static String createdCommentId;

  @Test
  @Order(1)
  void shouldCreateCommentViaMicroservice() {
    CommentResponse response =
        commentServiceClient.createComment(
            "Integration test comment", "user-1", "article-integration-1");

    assertThat(response).isNotNull();
    assertThat(response.getId()).isNotNull().isNotEmpty();
    assertThat(response.getBody()).isEqualTo("Integration test comment");
    assertThat(response.getUserId()).isEqualTo("user-1");
    assertThat(response.getArticleId()).isEqualTo("article-integration-1");
    assertThat(response.getCreatedAt()).isNotNull();

    createdCommentId = response.getId();
  }

  @Test
  @Order(2)
  void shouldGetCommentsByArticleIdViaMicroservice() {
    // Ensure the comment from the previous test is retrievable
    List<CommentResponse> comments =
        commentServiceClient.getCommentsByArticleId("article-integration-1");

    assertThat(comments).isNotEmpty();
    assertThat(comments).anyMatch(c -> c.getBody().equals("Integration test comment"));
  }

  @Test
  @Order(3)
  void shouldGetCommentByIdViaMicroservice() {
    assertThat(createdCommentId).isNotNull();

    Optional<CommentResponse> comment =
        commentServiceClient.getCommentById(createdCommentId, "article-integration-1");

    assertThat(comment).isPresent();
    assertThat(comment.get().getBody()).isEqualTo("Integration test comment");
    assertThat(comment.get().getUserId()).isEqualTo("user-1");
  }

  @Test
  @Order(4)
  void shouldGetCommentByIdWithoutArticleIdViaMicroservice() {
    assertThat(createdCommentId).isNotNull();

    Optional<CommentResponse> comment = commentServiceClient.getCommentById(createdCommentId);

    assertThat(comment).isPresent();
    assertThat(comment.get().getBody()).isEqualTo("Integration test comment");
  }

  @Test
  @Order(5)
  void shouldDeleteCommentViaMicroservice() {
    assertThat(createdCommentId).isNotNull();

    commentServiceClient.deleteComment(createdCommentId);

    Optional<CommentResponse> comment =
        commentServiceClient.getCommentById(createdCommentId, "article-integration-1");
    assertThat(comment).isEmpty();
  }

  @Test
  @Order(6)
  void shouldReturnEmptyForNonExistentComment() {
    Optional<CommentResponse> comment =
        commentServiceClient.getCommentById("non-existent-id", "article-integration-1");
    assertThat(comment).isEmpty();
  }

  @Test
  @Order(7)
  void shouldReturnEmptyListForArticleWithNoComments() {
    List<CommentResponse> comments =
        commentServiceClient.getCommentsByArticleId("article-with-no-comments");
    assertThat(comments).isEmpty();
  }

  @Test
  @Order(8)
  void shouldCreateMultipleCommentsForSameArticle() {
    commentServiceClient.createComment("First comment", "user-1", "article-multi-test");
    commentServiceClient.createComment("Second comment", "user-2", "article-multi-test");
    commentServiceClient.createComment("Third comment", "user-3", "article-multi-test");

    List<CommentResponse> comments =
        commentServiceClient.getCommentsByArticleId("article-multi-test");

    assertThat(comments).hasSize(3);
    assertThat(comments)
        .extracting(CommentResponse::getBody)
        .containsExactlyInAnyOrder("First comment", "Second comment", "Third comment");
  }
}
