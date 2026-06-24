package io.spring.infrastructure.repository;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.client.CommentServiceClient;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** {@link CommentRepository} backed by the comments microservice over HTTP. */
@Component
public class HttpCommentRepository implements CommentRepository {
  private final CommentServiceClient commentServiceClient;

  public HttpCommentRepository(CommentServiceClient commentServiceClient) {
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
