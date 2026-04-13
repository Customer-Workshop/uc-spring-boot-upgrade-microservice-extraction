package io.spring.infrastructure.service.comments;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

  public CommentResponseDto createComment(
      String id, String body, String userId, String articleId) {
    CreateCommentRequestDto request = new CreateCommentRequestDto(id, body, userId, articleId);
    ResponseEntity<CommentResponseDto> response =
        restTemplate.postForEntity(
            commentsServiceUrl + "/api/comments", request, CommentResponseDto.class);
    return response.getBody();
  }

  public List<CommentResponseDto> getCommentsByArticleId(String articleId) {
    ResponseEntity<CommentResponseDto[]> response =
        restTemplate.getForEntity(
            commentsServiceUrl + "/api/comments?articleId={articleId}",
            CommentResponseDto[].class,
            articleId);
    CommentResponseDto[] comments = response.getBody();
    return comments != null ? Arrays.asList(comments) : Collections.emptyList();
  }

  public Optional<CommentResponseDto> getCommentById(String id) {
    try {
      ResponseEntity<CommentResponseDto> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/{id}", CommentResponseDto.class, id);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public Optional<CommentResponseDto> getCommentByIdAndArticleId(String articleId, String id) {
    try {
      ResponseEntity<CommentResponseDto> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/by-article/{articleId}/{id}",
              CommentResponseDto.class,
              articleId,
              id);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public void deleteComment(String id) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/{id}", id);
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateCommentRequestDto {
    private String id;
    private String body;
    private String userId;
    private String articleId;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CommentResponseDto {
    private String id;
    private String body;
    private String articleId;
    private String userId;
    private Instant createdAt;
    private Instant updatedAt;
  }
}
