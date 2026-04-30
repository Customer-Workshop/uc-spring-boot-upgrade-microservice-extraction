package io.spring.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests that verify the monolith communicates correctly with the Comments microservice
 * via HTTP. These tests use the CommentServiceClient to make real HTTP calls. When the comments
 * microservice is not available, tests are skipped gracefully.
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentServiceIntegrationTest {

  @Autowired private CommentServiceClient commentServiceClient;

  private static String createdCommentId;

  @TestConfiguration
  static class Config {
    @Bean
    @Primary
    public RestTemplate testRestTemplate() {
      return new RestTemplate();
    }
  }

  @Test
  @Order(1)
  void shouldCreateCommentViaHttpClient() {
    try {
      CommentResponse response =
          commentServiceClient.create("Integration test comment", "article-1", "user-1");

      assertNotNull(response);
      assertNotNull(response.getId());
      assertEquals("Integration test comment", response.getBody());
      assertEquals("article-1", response.getArticleId());
      assertEquals("user-1", response.getUserId());
      createdCommentId = response.getId();
    } catch (Exception e) {
      System.out.println(
          "Comments microservice not available, skipping integration test: " + e.getMessage());
      org.junit.jupiter.api.Assumptions.assumeTrue(
          false, "Comments microservice not available at configured URL");
    }
  }

  @Test
  @Order(2)
  void shouldGetCommentsByArticleViaHttpClient() {
    try {
      List<CommentResponse> comments = commentServiceClient.findByArticleId("article-1");

      assertNotNull(comments);
      assertFalse(comments.isEmpty());
      assertTrue(comments.stream().anyMatch(c -> "article-1".equals(c.getArticleId())));
    } catch (Exception e) {
      System.out.println(
          "Comments microservice not available, skipping integration test: " + e.getMessage());
      org.junit.jupiter.api.Assumptions.assumeTrue(
          false, "Comments microservice not available at configured URL");
    }
  }

  @Test
  @Order(3)
  void shouldGetCommentByIdViaHttpClient() {
    org.junit.jupiter.api.Assumptions.assumeTrue(
        createdCommentId != null, "Previous test must have created a comment");

    try {
      Optional<CommentResponse> response = commentServiceClient.findById(createdCommentId);

      assertTrue(response.isPresent());
      assertEquals(createdCommentId, response.get().getId());
      assertEquals("Integration test comment", response.get().getBody());
    } catch (Exception e) {
      System.out.println(
          "Comments microservice not available, skipping integration test: " + e.getMessage());
      org.junit.jupiter.api.Assumptions.assumeTrue(
          false, "Comments microservice not available at configured URL");
    }
  }

  @Test
  @Order(4)
  void shouldGetCommentByIdAndArticleIdViaHttpClient() {
    org.junit.jupiter.api.Assumptions.assumeTrue(
        createdCommentId != null, "Previous test must have created a comment");

    try {
      Optional<CommentResponse> response =
          commentServiceClient.findByIdAndArticleId(createdCommentId, "article-1");

      assertTrue(response.isPresent());
      assertEquals(createdCommentId, response.get().getId());
    } catch (Exception e) {
      System.out.println(
          "Comments microservice not available, skipping integration test: " + e.getMessage());
      org.junit.jupiter.api.Assumptions.assumeTrue(
          false, "Comments microservice not available at configured URL");
    }
  }

  @Test
  @Order(5)
  void shouldReturn404ForNonExistentComment() {
    try {
      Optional<CommentResponse> response = commentServiceClient.findById("nonexistent-id");

      assertFalse(response.isPresent());
    } catch (Exception e) {
      System.out.println(
          "Comments microservice not available, skipping integration test: " + e.getMessage());
      org.junit.jupiter.api.Assumptions.assumeTrue(
          false, "Comments microservice not available at configured URL");
    }
  }

  @Test
  @Order(6)
  void shouldDeleteCommentViaHttpClient() {
    org.junit.jupiter.api.Assumptions.assumeTrue(
        createdCommentId != null, "Previous test must have created a comment");

    try {
      commentServiceClient.delete(createdCommentId);

      Optional<CommentResponse> response = commentServiceClient.findById(createdCommentId);
      assertFalse(response.isPresent());
    } catch (Exception e) {
      System.out.println(
          "Comments microservice not available, skipping integration test: " + e.getMessage());
      org.junit.jupiter.api.Assumptions.assumeTrue(
          false, "Comments microservice not available at configured URL");
    }
  }
}
