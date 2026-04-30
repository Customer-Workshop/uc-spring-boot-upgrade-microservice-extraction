package io.spring.comments.controller;

import io.spring.comments.controller.dto.CommentResponse;
import io.spring.comments.controller.dto.CreateCommentRequest;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/articles/{articleId}/comments")
public class CommentController {

  private final CommentRepository commentRepository;

  public CommentController(CommentRepository commentRepository) {
    this.commentRepository = commentRepository;
  }

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @PathVariable String articleId, @Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), articleId);
    commentRepository.save(comment);
    return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment));
  }

  @GetMapping
  public ResponseEntity<List<CommentResponse>> getComments(@PathVariable String articleId) {
    List<CommentResponse> comments =
        commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId).stream()
            .map(CommentResponse::from)
            .collect(Collectors.toList());
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/{commentId}")
  public ResponseEntity<CommentResponse> getComment(
      @PathVariable String articleId, @PathVariable String commentId) {
    Comment comment =
        commentRepository
            .findByIdAndArticleId(commentId, articleId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    return ResponseEntity.ok(CommentResponse.from(comment));
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable String articleId, @PathVariable String commentId) {
    Comment comment =
        commentRepository
            .findByIdAndArticleId(commentId, articleId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    commentRepository.delete(comment);
    return ResponseEntity.noContent().build();
  }
}
