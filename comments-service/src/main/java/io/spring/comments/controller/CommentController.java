package io.spring.comments.controller;

import io.spring.comments.model.Comment;
import io.spring.comments.repository.CommentRepository;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

  private final CommentRepository commentRepository;

  @PostMapping
  public ResponseEntity<Comment> createComment(@Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    Comment saved = commentRepository.save(comment);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @GetMapping
  public ResponseEntity<List<Comment>> getCommentsByArticleId(
      @RequestParam("articleId") String articleId) {
    List<Comment> comments = commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Comment> getCommentById(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = false) String articleId) {
    if (articleId != null && !articleId.isEmpty()) {
      return commentRepository
          .findByIdAndArticleId(id, articleId)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    }
    return commentRepository
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    if (commentRepository.existsById(id)) {
      commentRepository.deleteById(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  @Getter
  @Setter
  @NoArgsConstructor
  static class CreateCommentRequest {
    @NotBlank(message = "body is required")
    private String body;

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "articleId is required")
    private String articleId;
  }
}
