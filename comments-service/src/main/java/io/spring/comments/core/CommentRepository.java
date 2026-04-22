package io.spring.comments.core;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
  void save(Comment comment);

  Optional<Comment> findById(String id);

  Optional<Comment> findByArticleIdAndId(String articleId, String id);

  List<Comment> findByArticleId(String articleId);

  void remove(Comment comment);
}
