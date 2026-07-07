package io.spring.infrastructure.repository;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.rest.CommentServiceClient;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Comment persistence delegated over HTTP to the extracted comments microservice. The monolith no
 * longer owns the comments table.
 */
@Component
public class RestCommentRepository implements CommentRepository {
  private final CommentServiceClient commentServiceClient;

  @Autowired
  public RestCommentRepository(CommentServiceClient commentServiceClient) {
    this.commentServiceClient = commentServiceClient;
  }

  @Override
  public void save(Comment comment) {
    commentServiceClient.create(comment);
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    return commentServiceClient.findRawById(articleId, id);
  }

  @Override
  public void remove(Comment comment) {
    commentServiceClient.delete(comment.getId());
  }
}
