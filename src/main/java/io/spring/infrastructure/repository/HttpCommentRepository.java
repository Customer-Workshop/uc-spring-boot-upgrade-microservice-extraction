package io.spring.infrastructure.repository;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.service.CommentServiceClient;
import java.lang.reflect.Field;
import java.util.Optional;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class HttpCommentRepository implements CommentRepository {

  private final CommentServiceClient commentServiceClient;

  public HttpCommentRepository(CommentServiceClient commentServiceClient) {
    this.commentServiceClient = commentServiceClient;
  }

  @Override
  public void save(Comment comment) {
    commentServiceClient.createComment(
        comment.getId(), comment.getBody(), comment.getUserId(), comment.getArticleId());
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    return commentServiceClient.findById(id, articleId).map(HttpCommentRepository::toComment);
  }

  @Override
  public void remove(Comment comment) {
    commentServiceClient.deleteComment(comment.getId());
  }

  static Comment toComment(CommentServiceClient.CommentDto dto) {
    Comment comment = new Comment();
    try {
      setField(comment, "id", dto.getId());
      setField(comment, "body", dto.getBody());
      setField(comment, "userId", dto.getUserId());
      setField(comment, "articleId", dto.getArticleId());
      if (dto.getCreatedAt() != null) {
        setField(comment, "createdAt", new DateTime(dto.getCreatedAt().toEpochMilli()));
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to map comment DTO to domain object", e);
    }
    return comment;
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
