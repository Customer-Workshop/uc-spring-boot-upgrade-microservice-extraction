package io.spring.infrastructure.service.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
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

  public CommentResponse createComment(String articleId, String body, String userId) {
    String url = commentsServiceUrl + "/api/comments/articles/" + articleId;
    Map<String, String> request = new HashMap<>();
    request.put("body", body);
    request.put("userId", userId);
    ResponseEntity<Map<String, CommentResponse>> response =
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            new HttpEntity<>(request),
            new ParameterizedTypeReference<Map<String, CommentResponse>>() {});
    return response.getBody().get("comment");
  }

  public List<CommentResponse> getCommentsByArticleId(String articleId) {
    String url = commentsServiceUrl + "/api/comments/articles/" + articleId;
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

  public Optional<CommentResponse> getCommentByArticleIdAndId(String articleId, String commentId) {
    String url = commentsServiceUrl + "/api/comments/articles/" + articleId + "/" + commentId;
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

  public void deleteComment(String commentId) {
    String url = commentsServiceUrl + "/api/comments/" + commentId;
    restTemplate.delete(url);
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class CommentResponse {
    private String id;
    private String body;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("articleId")
    private String articleId;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;
  }
}
