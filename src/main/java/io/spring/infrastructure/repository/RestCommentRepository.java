package io.spring.infrastructure.repository;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("microservice")
public class RestCommentRepository implements CommentRepository {
  private static final Logger logger = LoggerFactory.getLogger(RestCommentRepository.class);

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;

  public RestCommentRepository(@Value("${services.comments.url}") String commentsServiceUrl) {
    this.restTemplate = new RestTemplate();
    this.commentsServiceUrl = commentsServiceUrl;
  }

  @Override
  public void save(Comment comment) {
    try {
      Map<String, String> body = new HashMap<>();
      body.put("body", comment.getBody());
      body.put("userId", comment.getUserId());
      body.put("articleId", comment.getArticleId());
      restTemplate.postForObject(commentsServiceUrl + "/api/comments", body, Map.class);
    } catch (RestClientException e) {
      logger.error("Failed to save comment to comments service: {}", e.getMessage());
      throw new RuntimeException("Failed to save comment via comments service", e);
    }
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    try {
      String url = commentsServiceUrl + "/api/comments/" + id + "?articleId=" + articleId;
      @SuppressWarnings("unchecked")
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      if (response != null && response.containsKey("comment")) {
        @SuppressWarnings("unchecked")
        Map<String, Object> commentMap = (Map<String, Object>) response.get("comment");
        Comment comment =
            new Comment(
                (String) commentMap.get("body"), (String) commentMap.get("userId"), articleId);
        return Optional.of(comment);
      }
      return Optional.empty();
    } catch (RestClientException e) {
      logger.warn("Failed to find comment '{}' from comments service: {}", id, e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public void remove(Comment comment) {
    try {
      restTemplate.delete(commentsServiceUrl + "/api/comments/" + comment.getId());
    } catch (RestClientException e) {
      logger.error("Failed to delete comment from comments service: {}", e.getMessage());
      throw new RuntimeException("Failed to delete comment via comments service", e);
    }
  }
}
