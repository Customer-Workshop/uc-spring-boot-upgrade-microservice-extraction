package io.spring.infrastructure.repository;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Repository
@Profile("microservice")
public class RemoteCommentRepository implements CommentRepository {
  private final RestTemplate restTemplate;
  private final String commentServiceUrl;

  public RemoteCommentRepository(
      RestTemplate restTemplate, @Value("${comment-service.url}") String commentServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentServiceUrl = commentServiceUrl;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void save(Comment comment) {
    try {
      Map<String, Object> body = new HashMap<>();
      Map<String, String> commentBody = new HashMap<>();
      commentBody.put("body", comment.getBody());
      body.put("comment", commentBody);

      restTemplate.postForEntity(
          commentServiceUrl + "/articles/{articleId}/comments",
          body,
          Map.class,
          comment.getArticleId());
    } catch (RestClientException e) {
      throw new RuntimeException(
          "Failed to save comment via comment-service: " + e.getMessage(), e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<Comment> findById(String articleId, String id) {
    try {
      ResponseEntity<Map> response =
          restTemplate.getForEntity(commentServiceUrl + "/internal/comments/{id}", Map.class, id);
      if (response.getBody() == null) {
        return Optional.empty();
      }
      Map<String, Object> commentMap = (Map<String, Object>) response.getBody().get("comment");
      if (commentMap == null) {
        return Optional.empty();
      }
      return Optional.of(
          new Comment(
              (String) commentMap.get("body"), (String) commentMap.get("userId"), articleId));
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }

  @Override
  public void remove(Comment comment) {
    try {
      restTemplate.delete(
          commentServiceUrl + "/articles/{articleId}/comments/{id}",
          comment.getArticleId(),
          comment.getId());
    } catch (RestClientException e) {
      throw new RuntimeException(
          "Failed to delete comment via comment-service: " + e.getMessage(), e);
    }
  }
}
