package io.spring.comments.integration;

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
class CommentsApiIntegrationTest {

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
  void shouldCreateAndRetrieveComment() {
    CreateCommentRequest request = new CreateCommentRequest();
    request.setBody("Integration test comment");
    request.setUserId("user-1");
    request.setArticleId("article-1");

    ResponseEntity<CommentDto> createResponse =
        restTemplate.postForEntity(baseUrl, request, CommentDto.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    CommentDto created = createResponse.getBody();
    assertThat(created).isNotNull();
    assertThat(created.getBody()).isEqualTo("Integration test comment");

    ResponseEntity<CommentDto> getResponse =
        restTemplate.getForEntity(
            baseUrl + "/" + created.getId() + "?articleId=article-1", CommentDto.class);

    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().getId()).isEqualTo(created.getId());
    assertThat(getResponse.getBody().getBody()).isEqualTo("Integration test comment");
  }

  @Test
  void shouldListCommentsByArticleId() {
    CreateCommentRequest req1 = new CreateCommentRequest();
    req1.setBody("Comment one");
    req1.setUserId("user-1");
    req1.setArticleId("article-42");
    restTemplate.postForEntity(baseUrl, req1, CommentDto.class);

    CreateCommentRequest req2 = new CreateCommentRequest();
    req2.setBody("Comment two");
    req2.setUserId("user-2");
    req2.setArticleId("article-42");
    restTemplate.postForEntity(baseUrl, req2, CommentDto.class);

    CreateCommentRequest req3 = new CreateCommentRequest();
    req3.setBody("Different article comment");
    req3.setUserId("user-1");
    req3.setArticleId("article-99");
    restTemplate.postForEntity(baseUrl, req3, CommentDto.class);

    ResponseEntity<List<CommentDto>> response =
        restTemplate.exchange(
            baseUrl + "?articleId=article-42",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<CommentDto>>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(2);
    assertThat(response.getBody()).allMatch(c -> c.getArticleId().equals("article-42"));
  }

  @Test
  void shouldDeleteCommentAndVerifyRemoval() {
    CreateCommentRequest request = new CreateCommentRequest();
    request.setBody("To be deleted");
    request.setUserId("user-1");
    request.setArticleId("article-1");

    ResponseEntity<CommentDto> createResponse =
        restTemplate.postForEntity(baseUrl, request, CommentDto.class);
    String commentId = createResponse.getBody().getId();

    assertThat(commentRepository.findById(commentId)).isPresent();

    restTemplate.delete(baseUrl + "/" + commentId + "?articleId=article-1");

    assertThat(commentRepository.findById(commentId)).isEmpty();
  }

  @Test
  void shouldReturn404ForNonExistentComment() {
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            baseUrl + "/nonexistent-id?articleId=article-1", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldCreateMultipleCommentsAndDeleteOne() {
    CreateCommentRequest req1 = new CreateCommentRequest();
    req1.setBody("First comment");
    req1.setUserId("user-1");
    req1.setArticleId("article-1");
    ResponseEntity<CommentDto> first =
        restTemplate.postForEntity(baseUrl, req1, CommentDto.class);

    CreateCommentRequest req2 = new CreateCommentRequest();
    req2.setBody("Second comment");
    req2.setUserId("user-2");
    req2.setArticleId("article-1");
    restTemplate.postForEntity(baseUrl, req2, CommentDto.class);

    restTemplate.delete(baseUrl + "/" + first.getBody().getId() + "?articleId=article-1");

    ResponseEntity<List<CommentDto>> response =
        restTemplate.exchange(
            baseUrl + "?articleId=article-1",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<CommentDto>>() {});

    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody().get(0).getBody()).isEqualTo("Second comment");
  }
}
