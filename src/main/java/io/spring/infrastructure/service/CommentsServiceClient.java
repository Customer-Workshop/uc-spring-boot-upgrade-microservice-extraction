package io.spring.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
public class CommentsServiceClient {

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;

  public CommentsServiceClient(
      RestTemplate restTemplate,
      @Value("${comments.service.url:http://localhost:8081}") String commentsServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
  }

  public CommentResponse createComment(String articleId, String body, String userId) {
    Map<String, String> request = Map.of("body", body, "userId", userId);
    ResponseEntity<Map<String, CommentResponse>> response =
        restTemplate.exchange(
            commentsServiceUrl + "/api/articles/{articleId}/comments",
            HttpMethod.POST,
            new HttpEntity<>(request),
            new ParameterizedTypeReference<Map<String, CommentResponse>>() {},
            articleId);
    Map<String, CommentResponse> responseBody = response.getBody();
    if (responseBody != null && responseBody.containsKey("comment")) {
      return responseBody.get("comment");
    }
    return null;
  }

  public List<CommentResponse> getCommentsByArticleId(String articleId) {
    try {
      ResponseEntity<Map<String, List<CommentResponse>>> response =
          restTemplate.exchange(
              commentsServiceUrl + "/api/articles/{articleId}/comments",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, List<CommentResponse>>>() {},
              articleId);
      Map<String, List<CommentResponse>> responseBody = response.getBody();
      if (responseBody != null && responseBody.containsKey("comments")) {
        return responseBody.get("comments");
      }
    } catch (HttpClientErrorException e) {
      return Collections.emptyList();
    }
    return Collections.emptyList();
  }

  public Optional<CommentResponse> getComment(String articleId, String commentId) {
    try {
      ResponseEntity<Map<String, CommentResponse>> response =
          restTemplate.exchange(
              commentsServiceUrl + "/api/articles/{articleId}/comments/{commentId}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, CommentResponse>>() {},
              articleId,
              commentId);
      Map<String, CommentResponse> responseBody = response.getBody();
      if (responseBody != null && responseBody.containsKey("comment")) {
        return Optional.ofNullable(responseBody.get("comment"));
      }
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
    return Optional.empty();
  }

  public void deleteComment(String articleId, String commentId) {
    restTemplate.delete(
        commentsServiceUrl + "/api/articles/{articleId}/comments/{commentId}",
        articleId,
        commentId);
  }

  @Data
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CommentResponse {
    private String id;
    private String body;
    private String articleId;
    private String userId;
    private DateTime createdAt;
    private DateTime updatedAt;
  }
}
