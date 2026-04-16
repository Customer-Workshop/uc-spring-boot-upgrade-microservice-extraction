package io.spring.comments.controller;

import io.spring.comments.model.Comment;
import io.spring.comments.repository.CommentRepository;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
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
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/comments")
public class CommentsController {

  private final CommentRepository commentRepository;

  public CommentsController(CommentRepository commentRepository) {
    this.commentRepository = commentRepository;
  }

  @PostMapping
  public ResponseEntity<CommentDto> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    commentRepository.save(comment);
    return ResponseEntity.status(HttpStatus.CREATED).body(toDto(comment));
  }

  @GetMapping
  public ResponseEntity<List<CommentDto>> getCommentsByArticleId(
      @RequestParam("articleId") String articleId) {
    List<Comment> comments = commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId);
    List<CommentDto> dtos = comments.stream().map(this::toDto).collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentDto> getCommentById(
      @PathVariable("id") String id, @RequestParam("articleId") String articleId) {
    Comment comment =
        commentRepository
            .findByIdAndArticleId(id, articleId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    return ResponseEntity.ok(toDto(comment));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable("id") String id, @RequestParam("articleId") String articleId) {
    Comment comment =
        commentRepository
            .findByIdAndArticleId(id, articleId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    commentRepository.delete(comment);
    return ResponseEntity.noContent().build();
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
