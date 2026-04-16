package io.spring.comments;

import static org.assertj.core.api.Assertions.assertThat;

import io.spring.comments.controller.CommentDto;
import io.spring.comments.controller.CreateCommentRequest;
import io.spring.comments.repository.CommentRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommentsServiceApplicationTests {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private CommentRepository commentRepository;

  private String baseUrl;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port + "/api/comments";
    commentRepository.deleteAll();
  }

  @Test
  void contextLoads() {}

  @Test
  void shouldCreateComment() {
    CreateCommentRequest request = new CreateCommentRequest();
    request.setBody("Test comment body");
    request.setUserId("user-1");
    request.setArticleId("article-1");

    ResponseEntity<CommentDto> response =
        restTemplate.postForEntity(baseUrl, request, CommentDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getBody()).isEqualTo("Test comment body");
    assertThat(response.getBody().getUserId()).isEqualTo("user-1");
    assertThat(response.getBody().getArticleId()).isEqualTo("article-1");
    assertThat(response.getBody().getId()).isNotNull();
  }

  @Test
  void shouldGetCommentsByArticleId() {
    CreateCommentRequest request1 = new CreateCommentRequest();
    request1.setBody("First comment");
    request1.setUserId("user-1");
    request1.setArticleId("article-1");
    restTemplate.postForEntity(baseUrl, request1, CommentDto.class);

    CreateCommentRequest request2 = new CreateCommentRequest();
    request2.setBody("Second comment");
    request2.setUserId("user-2");
    request2.setArticleId("article-1");
    restTemplate.postForEntity(baseUrl, request2, CommentDto.class);

    ResponseEntity<List<CommentDto>> response =
        restTemplate.exchange(
            baseUrl + "?articleId=article-1",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<CommentDto>>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(2);
  }

  @Test
  void shouldGetCommentById() {
    CreateCommentRequest request = new CreateCommentRequest();
    request.setBody("Test comment");
    request.setUserId("user-1");
    request.setArticleId("article-1");

    ResponseEntity<CommentDto> createResponse =
        restTemplate.postForEntity(baseUrl, request, CommentDto.class);
    String commentId = createResponse.getBody().getId();

    ResponseEntity<CommentDto> response =
        restTemplate.getForEntity(
            baseUrl + "/" + commentId + "?articleId=article-1", CommentDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getBody()).isEqualTo("Test comment");
  }

  @Test
  void shouldDeleteComment() {
    CreateCommentRequest request = new CreateCommentRequest();
    request.setBody("To be deleted");
    request.setUserId("user-1");
    request.setArticleId("article-1");

    ResponseEntity<CommentDto> createResponse =
        restTemplate.postForEntity(baseUrl, request, CommentDto.class);
    String commentId = createResponse.getBody().getId();

    restTemplate.delete(baseUrl + "/" + commentId + "?articleId=article-1");

    assertThat(commentRepository.findById(commentId)).isEmpty();
  }
}
