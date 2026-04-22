package io.spring.infrastructure.service;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Primary
public class CommentServiceClient implements CommentRepository {

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;

  public CommentServiceClient(
      RestTemplate restTemplate,
      @Value("${comments.service.url}") String commentsServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
  }

  @Override
  public void save(Comment comment) {
    CommentRequest request =
        new CommentRequest(comment.getBody(), comment.getUserId(), comment.getArticleId());
    ResponseEntity<CommentResponse> response =
        restTemplate.postForEntity(
            commentsServiceUrl + "/api/comments", request, CommentResponse.class);
    if (response.getBody() != null) {
      try {
        java.lang.reflect.Field idField = Comment.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(comment, response.getBody().getId());
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new RuntimeException("Failed to sync comment ID from microservice", e);
      }
    }
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    try {
      ResponseEntity<CommentResponse> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/by-article/{articleId}/{commentId}",
              CommentResponse.class,
              articleId,
              id);
      if (response.getBody() != null) {
        return Optional.of(response.getBody().toComment());
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public void remove(Comment comment) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/{id}", comment.getId());
  }
}
