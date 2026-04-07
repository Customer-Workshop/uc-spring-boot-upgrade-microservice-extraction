package io.spring.comments.controller;

import io.spring.comments.controller.dto.CommentResponse;
import io.spring.comments.controller.dto.CreateCommentRequest;
import io.spring.comments.model.Comment;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@AllArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @PathVariable("articleId") String articleId,
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment = commentService.createComment(request.getBody(), request.getUserId(), articleId);
    return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.fromComment(comment));
  }

  @GetMapping
  public ResponseEntity<List<CommentResponse>> getComments(
      @PathVariable("articleId") String articleId) {
    List<CommentResponse> comments =
        commentService.getCommentsByArticleId(articleId).stream()
            .map(CommentResponse::fromComment)
            .collect(Collectors.toList());
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/{commentId}")
  public ResponseEntity<CommentResponse> getComment(
      @PathVariable("articleId") String articleId,
      @PathVariable("commentId") String commentId) {
    return commentService
        .findByIdAndArticleId(commentId, articleId)
        .map(comment -> ResponseEntity.ok(CommentResponse.fromComment(comment)))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable("articleId") String articleId,
      @PathVariable("commentId") String commentId) {
    return commentService
        .findByIdAndArticleId(commentId, articleId)
        .map(
            comment -> {
              commentService.deleteComment(comment.getId());
              return ResponseEntity.noContent().<Void>build();
            })
        .orElse(ResponseEntity.notFound().build());
  }
}
