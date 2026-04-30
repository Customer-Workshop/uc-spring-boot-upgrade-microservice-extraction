package io.spring.infrastructure.service;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CommentServiceClient implements CommentRepository {

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;

  public CommentServiceClient(
      RestTemplate restTemplate, @Value("${comments.service.url}") String commentsServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
  }

  @Override
  public void save(Comment comment) {
    CommentRequest request = new CommentRequest();
    request.setBody(comment.getBody());
    request.setUserId(comment.getUserId());
    request.setArticleId(comment.getArticleId());
    restTemplate.postForEntity(
        commentsServiceUrl + "/api/comments", request, CommentResponse.class);
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    try {
      ResponseEntity<CommentResponse> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/{id}/article/{articleId}",
              CommentResponse.class,
              id,
              articleId);
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
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

  public List<CommentResponse> findByArticleId(String articleId) {
    try {
      ResponseEntity<CommentResponse[]> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/article/{articleId}",
              CommentResponse[].class,
              articleId);
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return Arrays.asList(response.getBody());
      }
      return Collections.emptyList();
    } catch (HttpClientErrorException e) {
      return Collections.emptyList();
    }
  }

  public Optional<CommentResponse> findResponseById(String id) {
    try {
      ResponseEntity<CommentResponse> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/{id}", CommentResponse.class, id);
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return Optional.of(response.getBody());
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }
}
