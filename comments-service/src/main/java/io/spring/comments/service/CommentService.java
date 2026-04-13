package io.spring.comments.service;

import io.spring.comments.model.CommentEntity;
import io.spring.comments.repository.CommentRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;

  public CommentEntity createComment(String id, String body, String userId, String articleId) {
    CommentEntity comment;
    if (id != null && !id.isBlank()) {
      comment = new CommentEntity(id, body, userId, articleId);
    } else {
      comment = new CommentEntity(body, userId, articleId);
    }
    return commentRepository.save(comment);
  }

  public Optional<CommentEntity> findById(String id) {
    return commentRepository.findById(id);
  }

  public List<CommentEntity> findByArticleId(String articleId) {
    return commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId);
  }

  public void deleteById(String id) {
    commentRepository.deleteById(id);
  }
}
