package io.spring.comments.controller;

import io.spring.comments.service.CommentService;
import java.util.List;
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
public class CommentController {

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<CommentDto> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    CommentDto comment =
        commentService.createComment(
            request.getBody(), request.getUserId(), request.getArticleId());
    return ResponseEntity.status(HttpStatus.CREATED).body(comment);
  }

  @GetMapping
  public ResponseEntity<List<CommentDto>> getCommentsByArticleId(
      @RequestParam("articleId") String articleId) {
    List<CommentDto> comments = commentService.getCommentsByArticleId(articleId);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentDto> getCommentById(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = false) String articleId) {
    if (articleId != null) {
      return commentService
          .getCommentById(id, articleId)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    }
    return commentService
        .getCommentById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    if (commentService.deleteComment(id)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }
}
