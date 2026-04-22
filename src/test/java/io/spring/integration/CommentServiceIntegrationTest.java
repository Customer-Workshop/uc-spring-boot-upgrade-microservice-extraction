package io.spring.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.spring.infrastructure.service.CommentRequest;
import io.spring.infrastructure.service.CommentResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests verifying the monolith can communicate with the comments microservice via HTTP.
 * These tests require the comments-service to be running on localhost:8081.
 *
 * <p>Run with: INTEGRATION_TEST=true docker-compose up comments-service, then execute these tests.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@EnabledIfEnvironmentVariable(named = "INTEGRATION_TEST", matches = "true")
class CommentServiceIntegrationTest {

  private static final String COMMENTS_SERVICE_URL = "http://localhost:8081";
  private static RestTemplate restTemplate;
  private static String createdCommentId;

  @BeforeAll
  static void setUp() {
    restTemplate = new RestTemplateBuilder().build();
  }

  @Test
  @Order(1)
  void should_create_comment_via_comments_service() {
    CommentRequest request = new CommentRequest("Integration test comment", "user-1", "article-1");

    ResponseEntity<CommentResponse> response =
        restTemplate.postForEntity(
            COMMENTS_SERVICE_URL + "/api/comments", request, CommentResponse.class);

    assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    assertThat(response.getBody(), notNullValue());
    assertThat(response.getBody().getBody(), equalTo("Integration test comment"));
    assertThat(response.getBody().getUserId(), equalTo("user-1"));
    assertThat(response.getBody().getArticleId(), equalTo("article-1"));
    assertThat(response.getBody().getId(), notNullValue());

    createdCommentId = response.getBody().getId();
  }

  @Test
  @Order(2)
  void should_get_comment_by_id_from_comments_service() {
    assertThat("Comment must be created first", createdCommentId, notNullValue());

    ResponseEntity<CommentResponse> response =
        restTemplate.getForEntity(
            COMMENTS_SERVICE_URL + "/api/comments/{id}", CommentResponse.class, createdCommentId);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
    assertThat(response.getBody().getId(), equalTo(createdCommentId));
    assertThat(response.getBody().getBody(), equalTo("Integration test comment"));
  }

  @Test
  @Order(3)
  void should_list_comments_by_article_id_from_comments_service() {
    ResponseEntity<CommentResponse[]> response =
        restTemplate.getForEntity(
            COMMENTS_SERVICE_URL + "/api/comments?articleId={articleId}",
            CommentResponse[].class,
            "article-1");

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
    assertThat(response.getBody().length >= 1, is(true));
  }

  @Test
  @Order(4)
  void should_get_comment_by_article_and_id_from_comments_service() {
    assertThat("Comment must be created first", createdCommentId, notNullValue());

    ResponseEntity<CommentResponse> response =
        restTemplate.getForEntity(
            COMMENTS_SERVICE_URL + "/api/comments/by-article/{articleId}/{commentId}",
            CommentResponse.class,
            "article-1",
            createdCommentId);

    assertThat(response.getStatusCode(), is(HttpStatus.OK));
    assertThat(response.getBody(), notNullValue());
    assertThat(response.getBody().getId(), equalTo(createdCommentId));
    assertThat(response.getBody().getArticleId(), equalTo("article-1"));
  }

  @Test
  @Order(5)
  void should_delete_comment_from_comments_service() {
    assertThat("Comment must be created first", createdCommentId, notNullValue());

    restTemplate.delete(COMMENTS_SERVICE_URL + "/api/comments/{id}", createdCommentId);

    try {
      restTemplate.getForEntity(
          COMMENTS_SERVICE_URL + "/api/comments/{id}", CommentResponse.class, createdCommentId);
    } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
      assertThat(e.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }
  }
}
