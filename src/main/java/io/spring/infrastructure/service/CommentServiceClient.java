package io.spring.infrastructure.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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

  public Optional<JsonNode> createComment(String articleId, String body, String userId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments";
    Map<String, String> request = new HashMap<>();
    request.put("body", body);
    request.put("userId", userId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

    try {
      ResponseEntity<JsonNode> response =
          restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
      return Optional.ofNullable(response.getBody());
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }

  public Optional<JsonNode> getCommentsByArticleId(String articleId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments";
    try {
      ResponseEntity<JsonNode> response =
          restTemplate.exchange(url, HttpMethod.GET, null, JsonNode.class);
      return Optional.ofNullable(response.getBody());
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }

  public Optional<JsonNode> getComment(String articleId, String commentId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments/" + commentId;
    try {
      ResponseEntity<JsonNode> response =
          restTemplate.exchange(url, HttpMethod.GET, null, JsonNode.class);
      return Optional.ofNullable(response.getBody());
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }

  public boolean deleteComment(String articleId, String commentId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments/" + commentId;
    try {
      restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
      return true;
    } catch (RestClientException e) {
      return false;
    }
  }
}
