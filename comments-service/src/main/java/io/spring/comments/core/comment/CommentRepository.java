package io.spring.comments.core.comment;

import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;

public interface CommentRepository {
  void save(Comment comment);

  Optional<Comment> findById(String id);

  Optional<Comment> findById(String articleId, String id);

  List<Comment> findByArticleId(String articleId);

  List<Comment> findByArticleIdWithCursor(
      String articleId, DateTime cursor, int limit, boolean next);

  void remove(Comment comment);
}
