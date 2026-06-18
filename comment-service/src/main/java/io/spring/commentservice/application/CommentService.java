package io.spring.commentservice.application;

import io.spring.commentservice.application.dto.CommentData;
import io.spring.commentservice.application.dto.ProfileDto;
import io.spring.commentservice.core.Comment;
import io.spring.commentservice.core.CommentRepository;
import io.spring.commentservice.infrastructure.client.MonolithClient;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
  private final CommentRepository commentRepository;
  private final MonolithClient monolithClient;

  public CommentService(CommentRepository commentRepository, MonolithClient monolithClient) {
    this.commentRepository = commentRepository;
    this.monolithClient = monolithClient;
  }

  public Comment createComment(String body, String userId, String articleId) {
    Comment comment = new Comment(body, userId, articleId);
    commentRepository.save(comment);
    return comment;
  }

  public Optional<CommentData> findById(String id) {
    return commentRepository.findById(id).map(this::toCommentData);
  }

  public List<CommentData> findByArticleId(String articleId) {
    return commentRepository.findByArticleId(articleId).stream()
        .map(this::toCommentData)
        .collect(Collectors.toList());
  }

  public void deleteComment(String id) {
    commentRepository.findById(id).ifPresent(commentRepository::remove);
  }

  private CommentData toCommentData(Comment comment) {
    ProfileDto profile = monolithClient.getUserProfile(comment.getUserId());
    return new CommentData(
        comment.getId(),
        comment.getBody(),
        comment.getArticleId(),
        comment.getCreatedAt(),
        comment.getCreatedAt(),
        profile);
  }
}
