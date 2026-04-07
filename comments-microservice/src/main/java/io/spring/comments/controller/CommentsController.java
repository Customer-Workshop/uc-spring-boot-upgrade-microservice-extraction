package io.spring.comments.controller;

import io.spring.comments.model.Comment;
import io.spring.comments.repository.CommentRepository;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
public class CommentsController {

  private final CommentRepository commentRepository;

  @PostMapping
  public ResponseEntity<CommentDto> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    commentRepository.save(comment);
    return ResponseEntity.status(HttpStatus.CREATED).body(toDto(comment));
  }

  @GetMapping("/article/{articleId}")
  public ResponseEntity<List<CommentDto>> getCommentsByArticleId(
      @PathVariable String articleId) {
    List<Comment> comments = commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId);
    List<CommentDto> dtos = comments.stream().map(this::toDto).collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentDto> getCommentById(
      @PathVariable String id,
      @RequestParam(required = false) String articleId) {
    return commentRepository
        .findById(id)
        .filter(c -> articleId == null || c.getArticleId().equals(articleId))
        .map(c -> ResponseEntity.ok(toDto(c)))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable String id) {
    if (commentRepository.existsById(id)) {
      commentRepository.deleteById(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  private CommentDto toDto(Comment comment) {
    return new CommentDto(
        comment.getId(),
        comment.getBody(),
        comment.getUserId(),
        comment.getArticleId(),
        comment.getCreatedAt(),
        comment.getUpdatedAt());
  }
}
