package io.spring.infrastructure.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
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

  public CommentResponse createComment(String body, String userId, String articleId) {
    CreateCommentRequest request = new CreateCommentRequest(body, userId, articleId);
    ResponseEntity<CommentResponse> response =
        restTemplate.postForEntity(
            commentsServiceUrl + "/api/comments", request, CommentResponse.class);
    return response.getBody();
  }

  public List<CommentResponse> getCommentsByArticleId(String articleId) {
    try {
      ResponseEntity<CommentResponse[]> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments?articleId=" + articleId, CommentResponse[].class);
      if (response.getBody() != null) {
        return Arrays.asList(response.getBody());
      }
    } catch (HttpClientErrorException e) {
      // fall through
    }
    return Collections.emptyList();
  }

  public Optional<CommentResponse> getComment(String id, String articleId) {
    try {
      ResponseEntity<CommentResponse> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/" + id + "?articleId=" + articleId,
              CommentResponse.class);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException e) {
      return Optional.empty();
    }
  }

  public Optional<CommentResponse> getComment(String id) {
    try {
      ResponseEntity<CommentResponse> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/" + id, CommentResponse.class);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException e) {
      return Optional.empty();
    }
  }

  public boolean deleteComment(String id) {
    try {
      restTemplate.delete(commentsServiceUrl + "/api/comments/" + id);
      return true;
    } catch (HttpClientErrorException e) {
      return false;
    }
  }

  public static class CreateCommentRequest {
    private String body;
    private String userId;
    private String articleId;

    public CreateCommentRequest(String body, String userId, String articleId) {
      this.body = body;
      this.userId = userId;
      this.articleId = articleId;
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

  public static class CommentResponse {
    private String id;
    private String body;
    private String userId;
    private String articleId;
    private String createdAt;
    private String updatedAt;

    public CommentResponse() {}

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

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public String getArticleId() {
      return articleId;
    }

    public void setArticleId(String articleId) {
      this.articleId = articleId;
    }

    public String getCreatedAt() {
      return createdAt;
    }

    public void setCreatedAt(String createdAt) {
      this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
      return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
      this.updatedAt = updatedAt;
    }
  }
}
