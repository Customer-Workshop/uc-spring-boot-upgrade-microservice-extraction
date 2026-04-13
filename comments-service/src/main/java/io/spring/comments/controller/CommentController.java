package io.spring.comments.controller;

import io.spring.comments.controller.dto.CommentResponse;
import io.spring.comments.controller.dto.CreateCommentRequest;
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
public class CommentController {

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    CommentEntity comment =
        commentService.createComment(
            request.getId(), request.getBody(), request.getUserId(), request.getArticleId());
    return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.fromEntity(comment));
  }

  @GetMapping
  public ResponseEntity<List<CommentResponse>> getCommentsByArticleId(
      @RequestParam("articleId") String articleId) {
    List<CommentResponse> comments =
        commentService.getCommentsByArticleId(articleId).stream()
            .map(CommentResponse::fromEntity)
            .collect(Collectors.toList());
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentResponse> getCommentById(@PathVariable("id") String id) {
    return commentService
        .getCommentById(id)
        .map(comment -> ResponseEntity.ok(CommentResponse.fromEntity(comment)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/by-article/{articleId}/{id}")
  public ResponseEntity<CommentResponse> getCommentByIdAndArticleId(
      @PathVariable("articleId") String articleId, @PathVariable("id") String id) {
    return commentService
        .getCommentByIdAndArticleId(id, articleId)
        .map(comment -> ResponseEntity.ok(CommentResponse.fromEntity(comment)))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    if (commentService.getCommentById(id).isPresent()) {
      commentService.deleteComment(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }
}
