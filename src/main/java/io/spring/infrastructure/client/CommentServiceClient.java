package io.spring.infrastructure.client;

import io.spring.core.comment.Comment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CommentServiceClient {

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;

  public CommentServiceClient(
      RestTemplate restTemplate, @Value("${comments.service.url}") String commentsServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
  }

  public Comment createComment(String body, String userId, String articleId) {
    Map<String, String> request = Map.of("body", body, "userId", userId, "articleId", articleId);
    Map<String, Object> response =
        restTemplate.postForObject(commentsServiceUrl + "/api/comments", request, Map.class);
    return mapToComment(response);
  }

  public Optional<Comment> findById(String id) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(commentsServiceUrl + "/api/comments/{id}", Map.class, id);
      if (response == null) {
        return Optional.empty();
      }
      return Optional.of(mapToComment(response));
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public Optional<Comment> findById(String articleId, String id) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(
              commentsServiceUrl + "/api/comments/{id}?articleId={articleId}",
              Map.class,
              id,
              articleId);
      if (response == null) {
        return Optional.empty();
      }
      return Optional.of(mapToComment(response));
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public List<Comment> findByArticleId(String articleId) {
    Map[] responses =
        restTemplate.getForObject(
            commentsServiceUrl + "/api/comments?articleId={articleId}", Map[].class, articleId);
    if (responses == null) {
      return Collections.emptyList();
    }
    List<Comment> comments = new ArrayList<>();
    for (Map<String, Object> response : responses) {
      comments.add(mapToComment(response));
    }
    return comments;
  }

  public void deleteComment(String id) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/{id}", id);
  }

  private Comment mapToComment(Map<String, Object> response) {
    return new Comment(
        (String) response.get("id"),
        (String) response.get("body"),
        (String) response.get("userId"),
        (String) response.get("articleId"),
        (String) response.get("createdAt"));
  }
}
