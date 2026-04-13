package io.spring.infrastructure.service.comments;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.util.Optional;
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
    return commentServiceClient
        .getCommentByIdAndArticleId(articleId, id)
        .map(
            dto -> {
              Comment comment = new Comment(dto.getBody(), dto.getUserId(), dto.getArticleId());
              // Use reflection to set id and createdAt since Comment has no setters
              try {
                java.lang.reflect.Field idField = Comment.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(comment, dto.getId());

                java.lang.reflect.Field createdAtField =
                    Comment.class.getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                createdAtField.set(
                    comment,
                    new org.joda.time.DateTime(dto.getCreatedAt().toEpochMilli()));
              } catch (Exception e) {
                throw new RuntimeException("Failed to map comment from service", e);
              }
              return comment;
            });
  }

  @Override
  public void remove(Comment comment) {
    commentServiceClient.deleteComment(comment.getId());
  }
}
