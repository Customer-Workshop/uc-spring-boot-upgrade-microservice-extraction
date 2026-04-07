package io.spring.comments.service;

import io.spring.comments.model.Comment;
import io.spring.comments.repository.CommentRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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

  public Optional<Comment> findById(String id) {
    return commentRepository.findById(id);
  }

  public Optional<Comment> findByIdAndArticleId(String id, String articleId) {
    return commentRepository.findByIdAndArticleId(id, articleId);
  }

  public void deleteComment(String id) {
    commentRepository.deleteById(id);
  }
}
