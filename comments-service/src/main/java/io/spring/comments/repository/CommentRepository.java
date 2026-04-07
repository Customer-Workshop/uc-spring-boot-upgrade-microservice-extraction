package io.spring.comments.repository;

import io.spring.comments.model.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

  List<Comment> findByArticleIdOrderByCreatedAtDesc(String articleId);

  Optional<Comment> findByIdAndArticleId(String id, String articleId);
}
