package io.spring.infrastructure.repository;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.comment.CommentResponseDto;
import io.spring.infrastructure.comment.CommentServiceClient;
import java.util.Optional;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

/**
 * {@link CommentRepository} backed by the extracted Comments microservice over HTTP. The monolith
 * no longer persists comments locally.
 */
@Component
public class RestCommentRepository implements CommentRepository {

  private final CommentServiceClient client;

  public RestCommentRepository(CommentServiceClient client) {
    this.client = client;
  }

  @Override
  public void save(Comment comment) {
    client.create(
        comment.getId(), comment.getArticleId(), comment.getUserId(), comment.getBody(), null);
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    return client
        .findById(id, null)
        .filter(dto -> articleId == null || articleId.equals(dto.getArticleId()))
        .map(RestCommentRepository::toComment);
  }

  @Override
  public void remove(Comment comment) {
    client.delete(comment.getId());
  }

  private static Comment toComment(CommentResponseDto dto) {
    DateTime createdAt =
        dto.getCreatedAt() == null ? new DateTime() : DateTime.parse(dto.getCreatedAt());
    return new Comment(dto.getId(), dto.getBody(), dto.getUserId(), dto.getArticleId(), createdAt);
  }
}
