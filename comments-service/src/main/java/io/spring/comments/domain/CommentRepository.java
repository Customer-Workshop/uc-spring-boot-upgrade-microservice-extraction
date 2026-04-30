package io.spring.comments.domain;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
  void save(Comment comment);

  Optional<Comment> findById(String id);

  Optional<Comment> findByIdAndArticleId(String id, String articleId);

  List<Comment> findByArticleId(String articleId);

  void remove(String id);
}
