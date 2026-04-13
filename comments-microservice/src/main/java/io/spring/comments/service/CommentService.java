package io.spring.comments.service;

import io.spring.comments.model.Comment;
import io.spring.comments.repository.CommentRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;

  public Comment createComment(String body, String userId, String articleId) {
    Comment comment = new Comment(body, userId, articleId);
    return commentRepository.save(comment);
  }

  public List<Comment> getCommentsByArticleId(String articleId) {
    return commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId);
  }

  public Optional<Comment> getCommentById(String id) {
    return commentRepository.findById(id);
  }

  @Transactional
  public boolean deleteComment(String id, String userId) {
    return commentRepository
        .findById(id)
        .map(
            comment -> {
              if (!comment.getUserId().equals(userId)) {
                return false;
              }
              commentRepository.delete(comment);
              return true;
            })
        .orElse(false);
  }

  @Transactional
  public void deleteCommentsByArticleId(String articleId) {
    commentRepository.deleteByArticleId(articleId);
  }
}
