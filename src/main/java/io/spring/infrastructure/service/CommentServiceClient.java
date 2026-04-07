package io.spring.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
      RestTemplate restTemplate,
      @Value("${comments.service.url:http://localhost:8081}") String commentsServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
  }

  public CommentResponse createComment(String body, String userId, String articleId) {
    String url = commentsServiceUrl + "/api/comments";
    Map<String, String> request = Map.of("body", body, "userId", userId, "articleId", articleId);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
    ResponseEntity<Map<String, CommentResponse>> response =
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<Map<String, CommentResponse>>() {});
    return response.getBody().get("comment");
  }

  public Optional<CommentResponse> findById(String id) {
    try {
      String url = commentsServiceUrl + "/api/comments/" + id;
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

  public Optional<CommentResponse> findByArticleIdAndId(String articleId, String id) {
    try {
      String url = commentsServiceUrl + "/api/comments/article/" + articleId + "/" + id;
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

  public List<CommentResponse> findByArticleId(String articleId) {
    try {
      String url = commentsServiceUrl + "/api/comments/article/" + articleId;
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

  public void deleteComment(String id) {
    String url = commentsServiceUrl + "/api/comments/" + id;
    restTemplate.delete(url);
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CommentResponse {
    private String id;
    private String body;
    private String userId;
    private String articleId;
    @JsonProperty("createdAt")
    private long createdAt;
    @JsonProperty("updatedAt")
    private long updatedAt;
  }
}
