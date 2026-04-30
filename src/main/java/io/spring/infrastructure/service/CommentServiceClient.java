package io.spring.infrastructure.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
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

  public CommentResponse createComment(String articleId, String body, String userId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments";
    CreateCommentRequest request = new CreateCommentRequest(body, userId);
    ResponseEntity<Map<String, CommentResponse>> response =
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            new HttpEntity<>(request),
            new ParameterizedTypeReference<Map<String, CommentResponse>>() {});
    return response.getBody().get("comment");
  }

  public List<CommentResponse> getCommentsByArticleId(String articleId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments";
    try {
      ResponseEntity<Map<String, List<CommentResponse>>> response =
          restTemplate.exchange(
              url,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, List<CommentResponse>>>() {});
      return response.getBody().get("comments");
    } catch (HttpClientErrorException.NotFound e) {
      return Collections.emptyList();
    }
  }

  public Optional<CommentResponse> getComment(String articleId, String commentId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments/" + commentId;
    try {
      ResponseEntity<Map<String, CommentResponse>> response =
          restTemplate.exchange(
              url,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, CommentResponse>>() {});
      return Optional.ofNullable(response.getBody().get("comment"));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public Optional<CommentResponse> getCommentById(String commentId) {
    String url = commentsServiceUrl + "/api/comments/" + commentId;
    try {
      ResponseEntity<Map<String, CommentResponse>> response =
          restTemplate.exchange(
              url,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, CommentResponse>>() {});
      return Optional.ofNullable(response.getBody().get("comment"));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public void deleteComment(String articleId, String commentId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments/" + commentId;
    restTemplate.delete(url);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateCommentRequest {
    private String body;
    private String userId;
  }

  @Data
  @NoArgsConstructor
  public static class CommentResponse {
    private String id;
    private String body;
    private String articleId;
    private String userId;
    private DateTime createdAt;
    private DateTime updatedAt;
  }
}
