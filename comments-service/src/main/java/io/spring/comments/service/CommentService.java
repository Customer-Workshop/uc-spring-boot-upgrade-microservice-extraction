package io.spring.comments.service;

import io.spring.comments.api.CommentRequest;
import io.spring.comments.api.CommentResponse;
import io.spring.comments.domain.Comment;
import io.spring.comments.repository.CommentRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;

  @Transactional
  public CommentResponse createComment(CommentRequest request) {
    Comment comment =
        new Comment(request.getId(), request.getBody(), request.getUserId(), request.getArticleId());
    comment = commentRepository.save(comment);
    return CommentResponse.fromEntity(comment);
  }

  public Optional<CommentResponse> findById(String id, String articleId) {
    return commentRepository.findByIdAndArticleId(id, articleId).map(CommentResponse::fromEntity);
  }

  public Optional<CommentResponse> findById(String id) {
    return commentRepository.findById(id).map(CommentResponse::fromEntity);
  }

  public List<CommentResponse> findByArticleId(String articleId) {
    return commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId).stream()
        .map(CommentResponse::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional
  public boolean deleteComment(String id) {
    if (commentRepository.existsById(id)) {
      commentRepository.deleteById(id);
      return true;
    }
    return false;
  }
}
