package io.spring.infrastructure.service;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CommentServiceClient implements CommentRepository {

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;

  public CommentServiceClient(
      RestTemplate restTemplate,
      @Value("${comments.service.url:http://localhost:8081}") String commentsServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
  }

  @Override
  public void save(Comment comment) {
    CommentRequest request = new CommentRequest();
    request.setBody(comment.getBody());
    request.setUserId(comment.getUserId());
    request.setArticleId(comment.getArticleId());

    CommentResponse response =
        restTemplate.postForObject(
            commentsServiceUrl + "/api/comments", request, CommentResponse.class);

    if (response != null) {
      comment.setId(response.getId());
    }
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    try {
      CommentResponse response =
          restTemplate.getForObject(
              commentsServiceUrl + "/api/comments/{id}?articleId={articleId}",
              CommentResponse.class,
              id,
              articleId);
      if (response == null) {
        return Optional.empty();
      }
      return Optional.of(toComment(response));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public void remove(Comment comment) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/{id}", comment.getId());
  }

  public Optional<CommentResponse> findResponseById(String id) {
    try {
      CommentResponse response =
          restTemplate.getForObject(
              commentsServiceUrl + "/api/comments/{id}", CommentResponse.class, id);
      return Optional.ofNullable(response);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public List<CommentResponse> findByArticleId(String articleId) {
    try {
      ResponseEntity<CommentResponse[]> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments?articleId={articleId}",
              CommentResponse[].class,
              articleId);
      if (response.getBody() == null) {
        return Collections.emptyList();
      }
      return Arrays.asList(response.getBody());
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  private Comment toComment(CommentResponse response) {
    Comment comment =
        new Comment(response.getBody(), response.getUserId(), response.getArticleId());
    comment.setId(response.getId());
    return comment;
  }
}
