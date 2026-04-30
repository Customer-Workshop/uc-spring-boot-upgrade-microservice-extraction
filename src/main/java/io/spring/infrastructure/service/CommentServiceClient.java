package io.spring.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CommentServiceClient implements CommentRepository {

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;

  public CommentServiceClient(
      @Value("${comments.service.url:http://localhost:8081}") String commentsServiceUrl) {
    this.restTemplate = new RestTemplate();
    this.commentsServiceUrl = commentsServiceUrl;
  }

  @Override
  public void save(Comment comment) {
    CreateCommentRequest request = new CreateCommentRequest();
    request.setId(comment.getId());
    request.setBody(comment.getBody());
    request.setUserId(comment.getUserId());
    request.setArticleId(comment.getArticleId());
    restTemplate.postForEntity(commentsServiceUrl + "/api/comments", request, CommentDto.class);
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    try {
      ResponseEntity<CommentDto> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/{id}?articleId={articleId}",
              CommentDto.class,
              id,
              articleId);
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return Optional.of(toDomainComment(response.getBody()));
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public void remove(Comment comment) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/{id}", comment.getId());
  }

  public List<CommentDto> getCommentsByArticleId(String articleId) {
    try {
      ResponseEntity<CommentDto[]> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments?articleId={articleId}",
              CommentDto[].class,
              articleId);
      if (response.getBody() != null) {
        return Arrays.asList(response.getBody());
      }
      return Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public Optional<CommentDto> getCommentById(String id) {
    try {
      ResponseEntity<CommentDto> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/{id}", CommentDto.class, id);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  private Comment toDomainComment(CommentDto dto) {
    return Comment.reconstitute(
        dto.getId(),
        dto.getBody(),
        dto.getUserId(),
        dto.getArticleId(),
        dto.getCreatedAtDateTime());
  }

  @Data
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CommentDto {
    private String id;
    private String body;
    private String userId;
    private String articleId;
    private String createdAt;
    private String updatedAt;

    public DateTime getCreatedAtDateTime() {
      if (createdAt != null) {
        try {
          return new DateTime(createdAt);
        } catch (Exception e) {
          return new DateTime();
        }
      }
      return new DateTime();
    }
  }

  @Data
  @NoArgsConstructor
  public static class CreateCommentRequest {
    private String id;
    private String body;
    private String userId;
    private String articleId;
  }
}
