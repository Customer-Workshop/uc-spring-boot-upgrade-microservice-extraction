package io.spring.infrastructure.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
      RestTemplate restTemplate,
      @Value("${comments.service.url:http://localhost:8081}") String commentsServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
  }

  public CommentResponse createComment(String body, String userId, String articleId) {
    CreateCommentRequest request = new CreateCommentRequest();
    request.setBody(body);
    request.setUserId(userId);
    request.setArticleId(articleId);
    return restTemplate.postForObject(
        commentsServiceUrl + "/api/comments", request, CommentResponse.class);
  }

  public List<CommentResponse> getCommentsByArticleId(String articleId) {
    CommentResponse[] comments =
        restTemplate.getForObject(
            commentsServiceUrl + "/api/comments/article/{articleId}",
            CommentResponse[].class,
            articleId);
    if (comments == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(comments);
  }

  public Optional<CommentResponse> getCommentById(String id) {
    try {
      CommentResponse comment =
          restTemplate.getForObject(
              commentsServiceUrl + "/api/comments/{id}", CommentResponse.class, id);
      return Optional.ofNullable(comment);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public Optional<CommentResponse> getCommentByIdAndArticleId(String id, String articleId) {
    try {
      CommentResponse comment =
          restTemplate.getForObject(
              commentsServiceUrl + "/api/comments/{id}/article/{articleId}",
              CommentResponse.class,
              id,
              articleId);
      return Optional.ofNullable(comment);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public void deleteComment(String id) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/{id}", id);
  }

  public static class CommentResponse {
    private String id;
    private String body;
    private String articleId;
    private String userId;
    private String createdAt;
    private String updatedAt;

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

  public static class CreateCommentRequest {
    private String body;
    private String userId;
    private String articleId;

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
  }
}
