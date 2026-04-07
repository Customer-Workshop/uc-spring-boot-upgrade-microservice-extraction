package io.spring.comments.api;

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
  public ResponseEntity<CommentResponse> createComment(
      @Valid @RequestBody CommentRequest request) {
    CommentResponse response = commentService.createComment(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<List<CommentResponse>> getCommentsByArticleId(
      @RequestParam("articleId") String articleId) {
    List<CommentResponse> comments = commentService.findByArticleId(articleId);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentResponse> getComment(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = false) String articleId) {
    if (articleId != null) {
      return commentService
          .findById(id, articleId)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    }
    return commentService
        .findById(id)
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
