package io.spring.core.comment;

import java.util.Optional;

public interface CommentRepository {
  /** Saves a comment. */
  void save(Comment comment);

  /** Finds a comment by article and comment identifiers. */
  Optional<Comment> findById(String articleId, String id);

  /** Removes a comment. */
  void remove(Comment comment);
}
