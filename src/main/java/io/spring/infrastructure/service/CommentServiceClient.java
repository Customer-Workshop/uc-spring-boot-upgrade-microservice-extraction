package io.spring.infrastructure.service;

import io.spring.core.comment.Comment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class CommentServiceClient {

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;

  public CommentServiceClient(
      RestTemplate restTemplate,
      @Value("${comments.service.url:http://localhost:8081}") String commentsServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
  }

  @SuppressWarnings("unchecked")
  public Comment createComment(String body, String userId, String articleId) {
    Map<String, String> request = Map.of("body", body, "userId", userId, "articleId", articleId);

    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            commentsServiceUrl + "/api/comments",
            HttpMethod.POST,
            new HttpEntity<>(request),
            new ParameterizedTypeReference<Map<String, Object>>() {});

    Map<String, Object> commentMap = (Map<String, Object>) response.getBody().get("comment");
    return mapToComment(commentMap);
  }

  @SuppressWarnings("unchecked")
  public List<Comment> getCommentsByArticleId(String articleId) {
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            commentsServiceUrl + "/api/comments?articleId=" + articleId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});

    List<Map<String, Object>> commentsList =
        (List<Map<String, Object>>) response.getBody().get("comments");
    if (commentsList == null) {
      return Collections.emptyList();
    }
    List<Comment> comments = new ArrayList<>();
    for (Map<String, Object> commentMap : commentsList) {
      comments.add(mapToComment(commentMap));
    }
    return comments;
  }

  @SuppressWarnings("unchecked")
  public Optional<Comment> getCommentByIdAndArticleId(String articleId, String commentId) {
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              commentsServiceUrl + "/api/comments/" + commentId + "?articleId=" + articleId,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {});

      Map<String, Object> commentMap = (Map<String, Object>) response.getBody().get("comment");
      return Optional.of(mapToComment(commentMap));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public void deleteComment(String commentId) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/" + commentId);
  }

  private Comment mapToComment(Map<String, Object> map) {
    return new Comment(
        (String) map.get("id"),
        (String) map.get("body"),
        (String) map.get("userId"),
        (String) map.get("articleId"),
        (String) map.get("createdAt"));
  }
}
