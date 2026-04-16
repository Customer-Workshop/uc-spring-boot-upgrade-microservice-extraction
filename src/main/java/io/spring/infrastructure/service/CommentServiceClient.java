package io.spring.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
    CreateCommentRequest request = new CreateCommentRequest();
    request.setBody(body);
    request.setUserId(userId);
    request.setArticleId(articleId);

    ResponseEntity<CommentResponse> response =
        restTemplate.postForEntity(
            commentsServiceUrl + "/api/comments", request, CommentResponse.class);
    return response.getBody();
  }

  public List<CommentResponse> getCommentsByArticleId(String articleId) {
    ResponseEntity<CommentResponse[]> response =
        restTemplate.getForEntity(
            commentsServiceUrl + "/api/comments?articleId=" + articleId, CommentResponse[].class);
    if (response.getBody() != null) {
      return Arrays.asList(response.getBody());
    }
    return Collections.emptyList();
  }

  public Optional<CommentResponse> getCommentById(String id, String articleId) {
    try {
      ResponseEntity<CommentResponse> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/" + id + "?articleId=" + articleId,
              CommentResponse.class);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public void deleteComment(String id, String articleId) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/" + id + "?articleId=" + articleId);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CommentResponse {
    private String id;
    private String body;
    private String userId;
    private String articleId;
    private Instant createdAt;
    private Instant updatedAt;
  }

  @Data
  @NoArgsConstructor
  public static class CreateCommentRequest {
    private String body;
    private String userId;
    private String articleId;
  }
}
