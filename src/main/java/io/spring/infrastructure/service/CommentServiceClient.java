package io.spring.infrastructure.service;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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
    Map<String, String> request =
        Map.of(
            "body", comment.getBody(),
            "userId", comment.getUserId(),
            "articleId", comment.getArticleId());
    restTemplate.postForEntity(commentsServiceUrl + "/api/comments", request, Map.class);
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              commentsServiceUrl + "/api/comments/" + id,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {});
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        Map<String, Object> body = response.getBody();
        String responseArticleId = (String) body.get("articleId");
        if (articleId != null && !articleId.equals(responseArticleId)) {
          return Optional.empty();
        }
        Comment comment =
            new Comment(
                (String) body.get("id"),
                (String) body.get("body"),
                (String) body.get("userId"),
                responseArticleId,
                new org.joda.time.DateTime());
        return Optional.of(comment);
      }
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
    return Optional.empty();
  }

  @Override
  public void remove(Comment comment) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/" + comment.getId());
  }

  public List<Map<String, Object>> findByArticleId(String articleId) {
    try {
      ResponseEntity<List<Map<String, Object>>> response =
          restTemplate.exchange(
              commentsServiceUrl + "/api/comments?articleId=" + articleId,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<Map<String, Object>>>() {});
      if (response.getBody() != null) {
        return response.getBody();
      }
    } catch (Exception e) {
      // Fall through to return empty list
    }
    return new ArrayList<>();
  }

  public Optional<Map<String, Object>> findCommentById(String id) {
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              commentsServiceUrl + "/api/comments/" + id,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {});
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return Optional.of(response.getBody());
      }
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
    return Optional.empty();
  }
}
