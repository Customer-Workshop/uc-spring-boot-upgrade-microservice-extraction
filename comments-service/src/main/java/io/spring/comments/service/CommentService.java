package io.spring.comments.service;

import io.spring.comments.controller.CommentDto;
import io.spring.comments.model.CommentEntity;
import io.spring.comments.repository.CommentJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommentService {

  private final CommentJpaRepository repository;

  public CommentDto createComment(String body, String userId, String articleId) {
    CommentEntity entity = new CommentEntity(body, userId, articleId);
    repository.save(entity);
    return toDto(entity);
  }

  public List<CommentDto> getCommentsByArticleId(String articleId) {
    return repository.findByArticleIdOrderByCreatedAtDesc(articleId).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  public Optional<CommentDto> getCommentById(String id, String articleId) {
    return repository.findByIdAndArticleId(id, articleId).map(this::toDto);
  }

  public Optional<CommentDto> getCommentById(String id) {
    return repository.findById(id).map(this::toDto);
  }

  public boolean deleteComment(String id) {
    return repository
        .findById(id)
        .map(
            entity -> {
              repository.delete(entity);
              return true;
            })
        .orElse(false);
  }

  private CommentDto toDto(CommentEntity entity) {
    return new CommentDto(
        entity.getId(),
        entity.getBody(),
        entity.getUserId(),
        entity.getArticleId(),
        entity.getCreatedAt().toString(),
        entity.getUpdatedAt().toString());
  }
}
