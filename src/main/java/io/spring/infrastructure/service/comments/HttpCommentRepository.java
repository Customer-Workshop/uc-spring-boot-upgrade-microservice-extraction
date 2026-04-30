package io.spring.infrastructure.service.comments;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.service.comments.CommentServiceClient.CommentServiceResponse;
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
    return commentServiceClient
        .getCommentByIdAndArticleId(id, articleId)
        .map(HttpCommentRepository::toComment);
  }

  @Override
  public void remove(Comment comment) {
    commentServiceClient.deleteComment(comment.getId());
  }

  private static Comment toComment(CommentServiceResponse response) {
    return new Comment(
        response.getId(),
        response.getBody(),
        response.getUserId(),
        response.getArticleId(),
        new DateTime(response.getCreatedAt().toEpochMilli()));
  }
}
