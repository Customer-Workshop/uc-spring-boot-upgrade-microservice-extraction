package io.spring.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.core.comment.Comment;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class CommentServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(CommentServiceClient.class);

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
    CreateCommentPayload payload = new CreateCommentPayload();
    payload.setBody(body);
    payload.setUserId(userId);
    payload.setArticleId(articleId);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<CreateCommentPayload> request = new HttpEntity<>(payload, headers);

    ResponseEntity<CommentResponse> response =
        restTemplate.postForEntity(url, request, CommentResponse.class);
    return response.getBody();
  }

  public List<CommentResponse> getCommentsByArticleId(String articleId) {
    String url = commentsServiceUrl + "/api/comments/article/" + articleId;
    try {
      ResponseEntity<CommentResponse[]> response =
          restTemplate.getForEntity(url, CommentResponse[].class);
      CommentResponse[] body = response.getBody();
      if (body != null) {
        return Arrays.asList(body);
      }
    } catch (HttpClientErrorException e) {
      logger.error("Error fetching comments for article {}: {}", articleId, e.getMessage());
    }
    return Collections.emptyList();
  }

  public Optional<CommentResponse> getCommentById(String id, String articleId) {
    String url = commentsServiceUrl + "/api/comments/" + id;
    if (articleId != null) {
      url += "?articleId=" + articleId;
    }
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

  public void deleteComment(String id) {
    String url = commentsServiceUrl + "/api/comments/" + id;
    restTemplate.delete(url);
  }

  @Data
  @NoArgsConstructor
  public static class CreateCommentPayload {
    private String body;
    private String userId;
    private String articleId;
  }

  @Data
  @NoArgsConstructor
  public static class CommentResponse {
    private String id;
    private String body;
    @JsonProperty("userId")
    private String userId;
    @JsonProperty("articleId")
    private String articleId;
    private String createdAt;
    private String updatedAt;
  }
}
