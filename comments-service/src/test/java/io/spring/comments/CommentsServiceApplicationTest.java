package io.spring.comments;

import static org.assertj.core.api.Assertions.assertThat;

import io.spring.comments.controller.dto.CommentResponse;
import io.spring.comments.controller.dto.CreateCommentRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CommentsServiceApplicationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  private String baseUrl() {
    return "http://localhost:" + port + "/api/articles";
  }

  @Test
  void contextLoads() {}

  @Test
  void shouldCreateAndRetrieveComment() {
    String articleId = "test-article-1";
    CreateCommentRequest request = new CreateCommentRequest("Great article!", "user-1");

    ResponseEntity<CommentResponse> createResponse =
        restTemplate.postForEntity(
            baseUrl() + "/" + articleId + "/comments", request, CommentResponse.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResponse.getBody()).isNotNull();
    assertThat(createResponse.getBody().getBody()).isEqualTo("Great article!");
    assertThat(createResponse.getBody().getUserId()).isEqualTo("user-1");
    assertThat(createResponse.getBody().getArticleId()).isEqualTo(articleId);

    ResponseEntity<List<CommentResponse>> listResponse =
        restTemplate.exchange(
            baseUrl() + "/" + articleId + "/comments",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<CommentResponse>>() {});

    assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResponse.getBody()).isNotNull();
    assertThat(listResponse.getBody()).hasSize(1);
    assertThat(listResponse.getBody().get(0).getBody()).isEqualTo("Great article!");
  }

  @Test
  void shouldDeleteComment() {
    String articleId = "test-article-2";
    CreateCommentRequest request = new CreateCommentRequest("To be deleted", "user-1");

    ResponseEntity<CommentResponse> createResponse =
        restTemplate.postForEntity(
            baseUrl() + "/" + articleId + "/comments", request, CommentResponse.class);

    assertThat(createResponse.getBody()).isNotNull();
    String commentId = createResponse.getBody().getId();

    restTemplate.delete(baseUrl() + "/" + articleId + "/comments/" + commentId);

    ResponseEntity<List<CommentResponse>> listResponse =
        restTemplate.exchange(
            baseUrl() + "/" + articleId + "/comments",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<CommentResponse>>() {});

    assertThat(listResponse.getBody()).isNotNull();
    assertThat(listResponse.getBody()).isEmpty();
  }

  @Test
  void shouldReturnEmptyListForArticleWithNoComments() {
    ResponseEntity<List<CommentResponse>> response =
        restTemplate.exchange(
            baseUrl() + "/nonexistent-article/comments",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<CommentResponse>>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).isEmpty();
  }
}
