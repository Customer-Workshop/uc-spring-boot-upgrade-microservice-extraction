package io.spring.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
    Map<String, String> request = new HashMap<>();
    request.put("body", body);
    request.put("userId", userId);
    ResponseEntity<CommentResponse> response =
        restTemplate.postForEntity(url, request, CommentResponse.class);
    return response.getBody();
  }

  public List<CommentResponse> getCommentsByArticleId(String articleId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments";
    ResponseEntity<List<CommentResponse>> response =
        restTemplate.exchange(
            url, HttpMethod.GET, null, new ParameterizedTypeReference<List<CommentResponse>>() {});
    return response.getBody() != null ? response.getBody() : Collections.emptyList();
  }

  public Optional<CommentResponse> getComment(String articleId, String commentId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments/" + commentId;
    try {
      ResponseEntity<CommentResponse> response =
          restTemplate.getForEntity(url, CommentResponse.class);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public void deleteComment(String articleId, String commentId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments/" + commentId;
    restTemplate.delete(url);
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CommentResponse {
    private String id;
    private String body;
    private String userId;
    private String articleId;
    private String createdAt;
    private String updatedAt;
  }
}
