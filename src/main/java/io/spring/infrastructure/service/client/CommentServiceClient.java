package io.spring.infrastructure.service.client;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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

  @SuppressWarnings("unchecked")
  public String saveAndReturnId(String body, String userId, String articleId) {
    Map<String, String> request = new HashMap<>();
    request.put("body", body);
    request.put("userId", userId);
    request.put("articleId", articleId);
    try {
      ResponseEntity<Map> response =
          restTemplate.postForEntity(commentsServiceUrl + "/api/comments", request, Map.class);
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        Map<String, Object> commentMap = (Map<String, Object>) response.getBody().get("comment");
        if (commentMap != null) {
          return (String) commentMap.get("id");
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to create comment in comments service", e);
    }
    throw new RuntimeException("Failed to create comment in comments service: no ID returned");
  }

  @Override
  public void save(Comment comment) {
    saveAndReturnId(comment.getBody(), comment.getUserId(), comment.getArticleId());
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
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> commentMap = (Map<String, Object>) response.getBody().get("comment");
        return Optional.ofNullable(mapToComment(commentMap));
      }
    } catch (Exception e) {
      return Optional.empty();
    }
    return Optional.empty();
  }

  @Override
  public void remove(Comment comment) {
    try {
      restTemplate.delete(commentsServiceUrl + "/api/comments/{id}", comment.getId());
    } catch (Exception e) {
      throw new RuntimeException("Failed to delete comment from comments service", e);
    }
  }

  @SuppressWarnings("unchecked")
  public List<CommentServiceResponse> findCommentsByArticleId(String articleId) {
    try {
      ResponseEntity<Map> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments?articleId={articleId}",
              Map.class,
              articleId);
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        List<Map<String, Object>> comments =
            (List<Map<String, Object>>) response.getBody().get("comments");
        if (comments == null) {
          return Collections.emptyList();
        }
        List<CommentServiceResponse> result = new ArrayList<>();
        for (Map<String, Object> commentMap : comments) {
          result.add(mapToResponse(commentMap));
        }
        return result;
      }
    } catch (Exception e) {
      return Collections.emptyList();
    }
    return Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  public Optional<CommentServiceResponse> findCommentById(String id) {
    try {
      ResponseEntity<Map> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/{id}", Map.class, id);
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        Map<String, Object> commentMap = (Map<String, Object>) response.getBody().get("comment");
        return Optional.ofNullable(mapToResponse(commentMap));
      }
    } catch (Exception e) {
      return Optional.empty();
    }
    return Optional.empty();
  }

  private Comment mapToComment(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    return new Comment(
        (String) map.get("body"), (String) map.get("userId"), (String) map.get("articleId"));
  }

  private CommentServiceResponse mapToResponse(Map<String, Object> map) {
    if (map == null) {
      return null;
    }
    CommentServiceResponse resp = new CommentServiceResponse();
    resp.setId((String) map.get("id"));
    resp.setBody((String) map.get("body"));
    resp.setArticleId((String) map.get("articleId"));
    resp.setUserId((String) map.get("userId"));
    resp.setCreatedAt((String) map.get("createdAt"));
    resp.setUpdatedAt((String) map.get("updatedAt"));
    return resp;
  }
}
