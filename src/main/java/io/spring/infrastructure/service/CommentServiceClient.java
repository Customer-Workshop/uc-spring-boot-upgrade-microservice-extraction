package io.spring.infrastructure.service;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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
    Map<String, String> request = new HashMap<>();
    request.put("id", comment.getId());
    request.put("body", comment.getBody());
    request.put("userId", comment.getUserId());
    request.put("articleId", comment.getArticleId());
    restTemplate.postForEntity(commentsServiceUrl + "/api/comments", request, Map.class);
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              commentsServiceUrl + "/api/comments/{id}?articleId={articleId}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {},
              id,
              articleId);
      return Optional.ofNullable(response.getBody()).map(this::mapToComment);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public void remove(Comment comment) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/{id}", comment.getId());
  }

  public List<Map<String, Object>> findByArticleId(String articleId) {
    ResponseEntity<List<Map<String, Object>>> response =
        restTemplate.exchange(
            commentsServiceUrl + "/api/comments?articleId={articleId}",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Map<String, Object>>>() {},
            articleId);
    return response.getBody() != null ? response.getBody() : new ArrayList<>();
  }

  public Optional<Map<String, Object>> findRawById(String id) {
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              commentsServiceUrl + "/api/comments/{id}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {},
              id);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  private Comment mapToComment(Map<String, Object> data) {
    Comment comment =
        new Comment(
            (String) data.get("body"), (String) data.get("userId"), (String) data.get("articleId"));
    return comment;
  }
}
