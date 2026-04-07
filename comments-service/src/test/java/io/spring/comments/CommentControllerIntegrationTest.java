package io.spring.comments;

import static org.assertj.core.api.Assertions.assertThat;

import io.spring.comments.model.Comment;
import io.spring.comments.repository.CommentRepository;
import java.util.List;
import java.util.Map;
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
class CommentControllerIntegrationTest {

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
  void shouldCreateComment() {
    Map<String, String> request =
        Map.of("body", "Test comment", "userId", "user-1", "articleId", "article-1");

    ResponseEntity<Comment> response =
        restTemplate.postForEntity(baseUrl, request, Comment.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getBody()).isEqualTo("Test comment");
    assertThat(response.getBody().getUserId()).isEqualTo("user-1");
    assertThat(response.getBody().getArticleId()).isEqualTo("article-1");
    assertThat(response.getBody().getId()).isNotNull();
  }

  @Test
  void shouldGetCommentsByArticleId() {
    Comment comment1 = new Comment("Comment 1", "user-1", "article-1");
    Comment comment2 = new Comment("Comment 2", "user-2", "article-1");
    Comment comment3 = new Comment("Comment 3", "user-1", "article-2");
    commentRepository.save(comment1);
    commentRepository.save(comment2);
    commentRepository.save(comment3);

    ResponseEntity<List<Comment>> response =
        restTemplate.exchange(
            baseUrl + "?articleId=article-1",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Comment>>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(2);
    assertThat(response.getBody()).extracting(Comment::getArticleId).containsOnly("article-1");
  }

  @Test
  void shouldGetCommentById() {
    Comment comment = new Comment("Test comment", "user-1", "article-1");
    commentRepository.save(comment);

    ResponseEntity<Comment> response =
        restTemplate.getForEntity(
            baseUrl + "/" + comment.getId() + "?articleId=article-1", Comment.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getBody()).isEqualTo("Test comment");
  }

  @Test
  void shouldGetCommentByIdWithoutArticleId() {
    Comment comment = new Comment("Test comment", "user-1", "article-1");
    commentRepository.save(comment);

    ResponseEntity<Comment> response =
        restTemplate.getForEntity(baseUrl + "/" + comment.getId(), Comment.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getBody()).isEqualTo("Test comment");
  }

  @Test
  void shouldReturn404ForNonExistentComment() {
    ResponseEntity<Comment> response =
        restTemplate.getForEntity(baseUrl + "/non-existent?articleId=article-1", Comment.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldDeleteComment() {
    Comment comment = new Comment("Test comment", "user-1", "article-1");
    commentRepository.save(comment);

    restTemplate.delete(baseUrl + "/" + comment.getId());

    assertThat(commentRepository.findById(comment.getId())).isEmpty();
  }

  @Test
  void shouldReturn404WhenDeletingNonExistentComment() {
    ResponseEntity<Void> response =
        restTemplate.exchange(
            baseUrl + "/non-existent", HttpMethod.DELETE, null, Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
