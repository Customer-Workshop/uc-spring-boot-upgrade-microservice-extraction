package io.spring.infrastructure.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
    CreateCommentRequest request = new CreateCommentRequest(body, userId, articleId);
    ResponseEntity<CommentResponse> response =
        restTemplate.postForEntity(
            commentsServiceUrl + "/api/comments", request, CommentResponse.class);
    return response.getBody();
  }

  public List<CommentResponse> getCommentsByArticleId(String articleId) {
    ResponseEntity<List<CommentResponse>> response =
        restTemplate.exchange(
            commentsServiceUrl + "/api/comments?articleId=" + articleId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<CommentResponse>>() {});
    List<CommentResponse> body = response.getBody();
    return body != null ? body : Collections.emptyList();
  }

  public Optional<CommentResponse> getCommentById(String id, String articleId) {
    try {
      ResponseEntity<CommentResponse> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/" + id + "?articleId=" + articleId,
              CommentResponse.class);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public Optional<CommentResponse> getCommentById(String id) {
    try {
      ResponseEntity<CommentResponse> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/" + id, CommentResponse.class);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public void deleteComment(String id) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/" + id);
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CommentResponse {
    private String id;
    private String body;
    private String userId;
    private String articleId;
    private String createdAt;
    private String updatedAt;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CreateCommentRequest {
    private String body;
    private String userId;
    private String articleId;
  }
}
