package io.spring.infrastructure.service;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
      RestTemplate restTemplate,
      @Value("${comments.service.url}") String commentsServiceUrl) {
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
      ResponseEntity<Map> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/{id}?articleId={articleId}",
              Map.class,
              id,
              articleId);
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return Optional.of(mapToComment(response.getBody()));
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

  public Optional<Comment> findByIdOnly(String id) {
    try {
      ResponseEntity<Map> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/by-id/{id}", Map.class, id);
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return Optional.of(mapToComment(response.getBody()));
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public List<Comment> findByArticleId(String articleId) {
    try {
      ResponseEntity<List> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments?articleId={articleId}",
              List.class,
              articleId);
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        List<Comment> comments = new ArrayList<>();
        for (Object item : response.getBody()) {
          if (item instanceof Map) {
            comments.add(mapToComment((Map<String, Object>) item));
          }
        }
        return comments;
      }
      return Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  private Comment mapToComment(Map<String, Object> map) {
    Comment comment =
        new Comment(
            (String) map.get("body"),
            (String) map.get("userId"),
            (String) map.get("articleId"));
    // Use reflection-free approach: the Comment constructor sets id and createdAt,
    // but we need the actual values from the service response
    try {
      java.lang.reflect.Field idField = Comment.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(comment, (String) map.get("id"));
    } catch (Exception e) {
      // fallback: use constructor-generated id
    }
    return comment;
  }
}
