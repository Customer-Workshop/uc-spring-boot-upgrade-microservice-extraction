package io.spring.interaction.core;

import java.util.Optional;

public interface CommentRepository {
  void save(Comment comment);

  Optional<Comment> findById(String articleId, String id);

  void remove(Comment comment);
}
