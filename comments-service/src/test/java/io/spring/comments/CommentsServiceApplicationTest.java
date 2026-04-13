package io.spring.comments;

import static org.assertj.core.api.Assertions.assertThat;

import io.spring.comments.controller.dto.CommentResponse;
import io.spring.comments.controller.dto.CreateCommentRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentsServiceApplicationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void contextLoads() {}

  @Test
  void shouldCreateAndRetrieveComment() {
    CreateCommentRequest request = new CreateCommentRequest("Test comment", "user-1", "article-1");

    ResponseEntity<CommentResponse> createResponse =
        restTemplate.postForEntity("/api/comments", request, CommentResponse.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResponse.getBody()).isNotNull();
    assertThat(createResponse.getBody().getBody()).isEqualTo("Test comment");

    String commentId = createResponse.getBody().getId();

    ResponseEntity<CommentResponse> getResponse =
        restTemplate.getForEntity("/api/comments/" + commentId, CommentResponse.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().getBody()).isEqualTo("Test comment");
  }

  @Test
  void shouldGetCommentsByArticleId() {
    CreateCommentRequest request1 =
        new CreateCommentRequest("Comment 1", "user-1", "article-get-test");
    CreateCommentRequest request2 =
        new CreateCommentRequest("Comment 2", "user-2", "article-get-test");

    restTemplate.postForEntity("/api/comments", request1, CommentResponse.class);
    restTemplate.postForEntity("/api/comments", request2, CommentResponse.class);

    ResponseEntity<List<CommentResponse>> response =
        restTemplate.exchange(
            "/api/comments?articleId=article-get-test",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<CommentResponse>>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  void shouldDeleteComment() {
    CreateCommentRequest request =
        new CreateCommentRequest("To delete", "user-1", "article-del-test");

    ResponseEntity<CommentResponse> createResponse =
        restTemplate.postForEntity("/api/comments", request, CommentResponse.class);
    String commentId = createResponse.getBody().getId();

    restTemplate.delete("/api/comments/" + commentId);

    ResponseEntity<CommentResponse> getResponse =
        restTemplate.getForEntity("/api/comments/" + commentId, CommentResponse.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
