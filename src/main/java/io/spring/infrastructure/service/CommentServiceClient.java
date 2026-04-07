package io.spring.infrastructure.service;

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

  public CommentDto createComment(String id, String body, String userId, String articleId) {
    CommentCreateRequest request = new CommentCreateRequest(id, body, userId, articleId);
    ResponseEntity<CommentDto> response =
        restTemplate.postForEntity(
            commentsServiceUrl + "/api/comments", request, CommentDto.class);
    return response.getBody();
  }

  public Optional<CommentDto> findById(String id, String articleId) {
    try {
      ResponseEntity<CommentDto> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/{id}?articleId={articleId}",
              CommentDto.class,
              id,
              articleId);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public Optional<CommentDto> findById(String id) {
    try {
      ResponseEntity<CommentDto> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/{id}", CommentDto.class, id);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public List<CommentDto> findByArticleId(String articleId) {
    ResponseEntity<CommentDto[]> response =
        restTemplate.getForEntity(
            commentsServiceUrl + "/api/comments?articleId={articleId}",
            CommentDto[].class,
            articleId);
    CommentDto[] body = response.getBody();
    if (body == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(body);
  }

  public void deleteComment(String id) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/{id}", id);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CommentDto {
    private String id;
    private String body;
    private String userId;
    private String articleId;
    private Instant createdAt;
    private Instant updatedAt;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CommentCreateRequest {
    private String id;
    private String body;
    private String userId;
    private String articleId;
  }
}
