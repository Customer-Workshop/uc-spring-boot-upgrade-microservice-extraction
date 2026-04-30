package io.spring.infrastructure.service.comments;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CommentServiceClient {

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;

  public CommentServiceClient(
      RestTemplate restTemplate,
      @Value("${comments.service.url:http://localhost:8081}") String commentsServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
  }

  public CommentServiceResponse createComment(
      String id, String body, String userId, String articleId) {
    CreateCommentPayload payload = new CreateCommentPayload(id, body, userId, articleId);
    return restTemplate.postForObject(
        commentsServiceUrl + "/api/comments", payload, CommentServiceResponse.class);
  }

  public List<CommentServiceResponse> getCommentsByArticleId(String articleId) {
    CommentServiceResponse[] response =
        restTemplate.getForObject(
            commentsServiceUrl + "/api/comments?articleId={articleId}",
            CommentServiceResponse[].class,
            articleId);
    return response != null ? Arrays.asList(response) : Collections.emptyList();
  }

  public Optional<CommentServiceResponse> getCommentByIdAndArticleId(String id, String articleId) {
    try {
      CommentServiceResponse response =
          restTemplate.getForObject(
              commentsServiceUrl + "/api/comments/{id}?articleId={articleId}",
              CommentServiceResponse.class,
              id,
              articleId);
      return Optional.ofNullable(response);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public Optional<CommentServiceResponse> getCommentById(String id) {
    try {
      CommentServiceResponse response =
          restTemplate.getForObject(
              commentsServiceUrl + "/api/comments/{id}", CommentServiceResponse.class, id);
      return Optional.ofNullable(response);
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public void deleteComment(String id) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/{id}", id);
  }

  public static class CreateCommentPayload {
    private String id;
    private String body;
    private String userId;
    private String articleId;

    public CreateCommentPayload() {}

    public CreateCommentPayload(String id, String body, String userId, String articleId) {
      this.id = id;
      this.body = body;
      this.userId = userId;
      this.articleId = articleId;
    }

    public String getId() {
      return id;
    }

    public String getBody() {
      return body;
    }

    public String getUserId() {
      return userId;
    }

    public String getArticleId() {
      return articleId;
    }
  }

  public static class CommentServiceResponse {
    private String id;
    private String body;
    private String articleId;
    private String userId;
    private Instant createdAt;
    private Instant updatedAt;

    public CommentServiceResponse() {}

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getBody() {
      return body;
    }

    public void setBody(String body) {
      this.body = body;
    }

    public String getArticleId() {
      return articleId;
    }

    public void setArticleId(String articleId) {
      this.articleId = articleId;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public Instant getCreatedAt() {
      return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
      this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
      return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
      this.updatedAt = updatedAt;
    }
  }
}
