package io.spring.comments.controller;

import io.spring.comments.model.CommentEntity;
import io.spring.comments.service.CommentService;
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

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<CommentDTO> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    CommentEntity comment =
        commentService.createComment(
            request.getId(), request.getBody(), request.getUserId(), request.getArticleId());
    return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(comment));
  }

  @GetMapping
  public ResponseEntity<List<CommentDTO>> getCommentsByArticleId(
      @RequestParam("articleId") String articleId) {
    List<CommentDTO> comments =
        commentService.getCommentsByArticleId(articleId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentDTO> getComment(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = false) String articleId) {
    if (articleId != null) {
      return commentService
          .getCommentByIdAndArticleId(id, articleId)
          .map(c -> ResponseEntity.ok(toDTO(c)))
          .orElse(ResponseEntity.notFound().build());
    }
    return commentService
        .getCommentById(id)
        .map(c -> ResponseEntity.ok(toDTO(c)))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    if (commentService.getCommentById(id).isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    commentService.deleteComment(id);
    return ResponseEntity.noContent().build();
  }

  private CommentDTO toDTO(CommentEntity entity) {
    return new CommentDTO(
        entity.getId(),
        entity.getBody(),
        entity.getUserId(),
        entity.getArticleId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
