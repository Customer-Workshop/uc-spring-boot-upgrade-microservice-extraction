package io.spring.core.comment;

import java.util.Optional;

/** Repository interface for {@link Comment} entity persistence. */
public interface CommentRepository {

  /**
   * Persists a new comment.
   *
   * @param comment the comment entity to save
   */
  void save(Comment comment);

  /**
   * Finds a comment by article and comment identifiers.
   *
   * @param articleId the article's unique identifier
   * @param id the comment's unique identifier
   * @return the comment, or empty if not found
   */
  Optional<Comment> findById(String articleId, String id);

  /**
   * Removes a comment from the data store.
   *
   * @param comment the comment entity to remove
   */
  void remove(Comment comment);
}
