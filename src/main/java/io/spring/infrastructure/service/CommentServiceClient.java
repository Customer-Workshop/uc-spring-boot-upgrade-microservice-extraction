package io.spring.infrastructure.service;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
      @Value("${comments.service.url:http://localhost:8081}") String commentsServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
  }

  @Override
  public void save(Comment comment) {
    String url = commentsServiceUrl + "/api/comments";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, String> body = new HashMap<>();
    body.put("id", comment.getId());
    body.put("body", comment.getBody());
    body.put("userId", comment.getUserId());
    body.put("articleId", comment.getArticleId());

    HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
    restTemplate.postForEntity(url, request, Map.class);
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    String url = commentsServiceUrl + "/api/comments/article/" + articleId + "/" + id;
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});
      Map<String, Object> responseBody = response.getBody();
      if (responseBody != null && responseBody.containsKey("comment")) {
        @SuppressWarnings("unchecked")
        Map<String, Object> commentMap = (Map<String, Object>) responseBody.get("comment");
        return Optional.of(mapToComment(commentMap));
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public void remove(Comment comment) {
    String url = commentsServiceUrl + "/api/comments/" + comment.getId();
    restTemplate.delete(url);
  }

  public Optional<Map<String, Object>> findRawById(String id) {
    String url = commentsServiceUrl + "/api/comments/" + id;
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});
      Map<String, Object> responseBody = response.getBody();
      if (responseBody != null && responseBody.containsKey("comment")) {
        @SuppressWarnings("unchecked")
        Map<String, Object> commentMap = (Map<String, Object>) responseBody.get("comment");
        return Optional.of(commentMap);
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public List<Map<String, Object>> findRawByArticleId(String articleId) {
    String url = commentsServiceUrl + "/api/comments/article/" + articleId;
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});
      Map<String, Object> responseBody = response.getBody();
      if (responseBody != null && responseBody.containsKey("comments")) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> commentsList =
            (List<Map<String, Object>>) responseBody.get("comments");
        return commentsList;
      }
      return new ArrayList<>();
    } catch (HttpClientErrorException.NotFound e) {
      return new ArrayList<>();
    }
  }

  private Comment mapToComment(Map<String, Object> map) {
    String createdAtStr = (String) map.get("createdAt");
    DateTime createdAt = createdAtStr != null ? DateTime.parse(createdAtStr) : new DateTime();
    return new Comment(
        (String) map.get("id"),
        (String) map.get("body"),
        (String) map.get("userId"),
        (String) map.get("articleId"),
        createdAt);
  }
}
