package io.spring.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Primary
public class CommentServiceClient implements CommentRepository {

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;

  public CommentServiceClient(
      RestTemplate restTemplate,
      @Value("${comments.service.url:http://localhost:8081}") String commentsServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
  }

  @Override
  public void save(Comment comment) {
    Map<String, String> request =
        Map.of(
            "body", comment.getBody(),
            "userId", comment.getUserId(),
            "articleId", comment.getArticleId());
    restTemplate.postForEntity(
        commentsServiceUrl + "/api/comments", new HttpEntity<>(request), Map.class);
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
      return Optional.ofNullable(response.getBody()).map(CommentDto::toComment);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  public Optional<Comment> findByCommentId(String id) {
    try {
      ResponseEntity<CommentDto> response =
          restTemplate.getForEntity(
              commentsServiceUrl + "/api/comments/{id}", CommentDto.class, id);
      return Optional.ofNullable(response.getBody()).map(CommentDto::toComment);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Optional.empty();
      }
      throw e;
    }
  }

  @Override
  public void remove(Comment comment) {
    restTemplate.delete(commentsServiceUrl + "/api/comments/{id}", comment.getId());
  }

  public List<Comment> findByArticleId(String articleId) {
    ResponseEntity<CommentDto[]> response =
        restTemplate.getForEntity(
            commentsServiceUrl + "/api/comments?articleId={articleId}",
            CommentDto[].class,
            articleId);
    CommentDto[] body = response.getBody();
    if (body == null) {
      return List.of();
    }
    return Arrays.stream(body).map(CommentDto::toComment).collect(Collectors.toList());
  }

  /** DTO for deserializing comments from the microservice REST API. */
  @Getter
  @Setter
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class CommentDto {
    private String id;
    private String body;
    private String userId;
    private String articleId;
    private String createdAt;
    private String updatedAt;

    Comment toComment() {
      Comment comment = new Comment();
      try {
        java.lang.reflect.Field idField = Comment.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(comment, this.id);

        java.lang.reflect.Field bodyField = Comment.class.getDeclaredField("body");
        bodyField.setAccessible(true);
        bodyField.set(comment, this.body);

        java.lang.reflect.Field userIdField = Comment.class.getDeclaredField("userId");
        userIdField.setAccessible(true);
        userIdField.set(comment, this.userId);

        java.lang.reflect.Field articleIdField = Comment.class.getDeclaredField("articleId");
        articleIdField.setAccessible(true);
        articleIdField.set(comment, this.articleId);

        java.lang.reflect.Field createdAtField = Comment.class.getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        if (this.createdAt != null) {
          createdAtField.set(comment, DateTime.parse(this.createdAt));
        }
      } catch (Exception e) {
        throw new RuntimeException("Failed to map CommentDto to Comment", e);
      }
      return comment;
    }
  }
}
