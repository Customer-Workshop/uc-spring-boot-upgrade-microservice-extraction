package io.spring.infrastructure.repository;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
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
    commentServiceClient.createComment(comment.getBody(), comment.getUserId(), comment.getArticleId());
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    return commentServiceClient
        .getCommentById(id, articleId)
        .map(HttpCommentRepository::toComment);
  }

  @Override
  public void remove(Comment comment) {
    commentServiceClient.deleteComment(comment.getId());
  }

  private static Comment toComment(CommentResponse response) {
    Comment comment = new Comment(response.getBody(), response.getUserId(), response.getArticleId());
    return comment;
  }
}
