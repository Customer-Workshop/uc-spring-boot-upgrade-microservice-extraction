package io.spring.comments.core;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
  void save(Comment comment);

  Optional<Comment> findById(String articleId, String id);

  Optional<Comment> findById(String id);

  void remove(Comment comment);

  List<Comment> findByArticleId(String articleId);
}
