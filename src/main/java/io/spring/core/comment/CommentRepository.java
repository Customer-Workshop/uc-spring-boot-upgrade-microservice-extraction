package io.spring.core.comment;

// TODO: CommentRepository has been extracted to the Comment Service (comment-service/).
// This interface should be removed once the Comment Service is fully deployed.

import java.util.Optional;

public interface CommentRepository {
  void save(Comment comment);

  Optional<Comment> findById(String articleId, String id);

  void remove(Comment comment);
}
