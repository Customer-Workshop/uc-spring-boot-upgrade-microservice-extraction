package io.spring.infrastructure.service;

import io.spring.core.comment.Comment;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  public CommentResponse create(String body, String articleId, String userId) {
    String url = commentsServiceUrl + "/api/comments";
    Map<String, String> request = Map.of("body", body, "articleId", articleId, "userId", userId);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
    ResponseEntity<CommentResponse> response =
        restTemplate.postForEntity(url, entity, CommentResponse.class);
    return response.getBody();
  }

  public List<CommentResponse> findByArticleId(String articleId) {
    String url = commentsServiceUrl + "/api/comments?articleId=" + articleId;
    ResponseEntity<CommentResponse[]> response =
        restTemplate.getForEntity(url, CommentResponse[].class);
    if (response.getBody() == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(response.getBody());
  }

  public Optional<CommentResponse> findById(String id) {
    String url = commentsServiceUrl + "/api/comments/" + id;
    try {
      ResponseEntity<CommentResponse> response =
          restTemplate.getForEntity(url, CommentResponse.class);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public Optional<CommentResponse> findByIdAndArticleId(String id, String articleId) {
    String url = commentsServiceUrl + "/api/comments/" + id + "?articleId=" + articleId;
    try {
      ResponseEntity<CommentResponse> response =
          restTemplate.getForEntity(url, CommentResponse.class);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public void delete(String id) {
    String url = commentsServiceUrl + "/api/comments/" + id;
    restTemplate.delete(url);
  }

  public static class CommentResponse {
    private String id;
    private String body;
    private String articleId;
    private String userId;
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

    public Comment toComment() {
      Comment c = new Comment(this.body, this.userId, this.articleId);
      return c;
    }
  }
}
