package io.spring.comments.service;

import io.spring.comments.model.CommentEntity;
import io.spring.comments.repository.CommentJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentService {

  private final CommentJpaRepository commentRepository;

  public CommentEntity createComment(String id, String body, String userId, String articleId) {
    String commentId = (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
    CommentEntity comment = new CommentEntity(commentId, body, userId, articleId);
    return commentRepository.save(comment);
  }

  public List<CommentEntity> getCommentsByArticleId(String articleId) {
    return commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId);
  }

  public Optional<CommentEntity> getCommentById(String id) {
    return commentRepository.findById(id);
  }

  public Optional<CommentEntity> getCommentByIdAndArticleId(String id, String articleId) {
    return commentRepository.findByIdAndArticleId(id, articleId);
  }

  public void deleteComment(String id) {
    commentRepository.deleteById(id);
  }
}
