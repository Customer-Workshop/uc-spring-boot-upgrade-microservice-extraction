package io.spring.comments.repository;

import io.spring.comments.model.CommentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentJpaRepository extends JpaRepository<CommentEntity, String> {

  List<CommentEntity> findByArticleIdOrderByCreatedAtDesc(String articleId);

  Optional<CommentEntity> findByIdAndArticleId(String id, String articleId);
}
