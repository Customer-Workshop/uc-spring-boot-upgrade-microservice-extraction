package io.spring.infrastructure.repository;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.service.CommentServiceClient;
import java.util.Optional;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

@Component
public class MyBatisCommentRepository implements CommentRepository {
  private final CommentServiceClient commentServiceClient;

  public MyBatisCommentRepository(CommentServiceClient commentServiceClient) {
    this.commentServiceClient = commentServiceClient;
  }

  @Override
  public void save(Comment comment) {
    commentServiceClient.createComment(
        comment.getBody(), comment.getUserId(), comment.getArticleId());
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    return commentServiceClient
        .getCommentById(id, articleId)
        .map(MyBatisCommentRepository::toComment);
  }

  @Override
  public void remove(Comment comment) {
    commentServiceClient.deleteComment(comment.getId());
  }

  static Comment toComment(CommentServiceClient.CommentResponse response) {
    Comment comment =
        new Comment(response.getBody(), response.getUserId(), response.getArticleId());
    try {
      java.lang.reflect.Field idField = Comment.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(comment, response.getId());
      java.lang.reflect.Field createdAtField = Comment.class.getDeclaredField("createdAt");
      createdAtField.setAccessible(true);
      createdAtField.set(comment, DateTime.parse(response.getCreatedAt()));
    } catch (Exception e) {
      // fallback: the constructor-generated id/createdAt remain
    }
    return comment;
  }
}
