package io.spring.infrastructure.service;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
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
    Map<String, String> request = new HashMap<>();
    request.put("body", comment.getBody());
    request.put("userId", comment.getUserId());
    request.put("articleId", comment.getArticleId());
    restTemplate.postForEntity(commentsServiceUrl + "/api/comments", request, Map.class);
  }

  @SuppressWarnings("unchecked")
  public String saveAndReturnId(Comment comment) {
    Map<String, String> request = new HashMap<>();
    request.put("body", comment.getBody());
    request.put("userId", comment.getUserId());
    request.put("articleId", comment.getArticleId());
    ResponseEntity<Map> response =
        restTemplate.postForEntity(commentsServiceUrl + "/api/comments", request, Map.class);
    Map<String, Object> body = response.getBody();
    return body != null ? (String) body.get("id") : null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Comment> findById(String articleId, String id) {
    try {
      ResponseEntity<Map> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/by-article-and-id?articleId={articleId}&id={id}",
              Map.class,
              articleId,
              id);
      return Optional.ofNullable(response.getBody())
          .map(m -> mapToComment((Map<String, Object>) m));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @SuppressWarnings("unchecked")
  public List<Comment> findByArticleId(String articleId) {
    try {
      ResponseEntity<List> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments?articleId={articleId}", List.class, articleId);
      List<?> body = response.getBody();
      if (body == null) {
        return Collections.emptyList();
      }
      return ((List<Map<String, Object>>) (List<?>) body)
          .stream().map(this::mapToComment).collect(Collectors.toList());
    } catch (HttpClientErrorException.NotFound e) {
      return Collections.emptyList();
    }
  }

  @SuppressWarnings("unchecked")
  public Optional<Comment> findByIdDirect(String id) {
    try {
      ResponseEntity<Map> response =
          restTemplate.getForEntity(commentsServiceUrl + "/api/comments/{id}", Map.class, id);
      return Optional.ofNullable(response.getBody())
          .map(m -> mapToComment((Map<String, Object>) m));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public void remove(Comment comment) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/{id}", comment.getId());
  }

  private Comment mapToComment(Map<String, Object> map) {
    return new Comment(
        (String) map.get("id"),
        (String) map.get("body"),
        (String) map.get("userId"),
        (String) map.get("articleId"),
        DateTime.parse((String) map.get("createdAt")));
  }
}
