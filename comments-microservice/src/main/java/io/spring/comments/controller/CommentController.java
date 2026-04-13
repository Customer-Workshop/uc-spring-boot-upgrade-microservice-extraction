package io.spring.comments.controller;

import io.spring.comments.model.Comment;
import io.spring.comments.service.CommentService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles/{articleId}/comments")
@AllArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<Map<String, Object>> createComment(
      @PathVariable String articleId,
      @RequestHeader("X-User-Id") String userId,
      @Valid @RequestBody NewCommentRequest request) {
    Comment comment = commentService.createComment(request.getBody(), userId, articleId);
    Map<String, Object> response = new HashMap<>();
    response.put("comment", comment);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> getComments(@PathVariable String articleId) {
    List<Comment> comments = commentService.getCommentsByArticleId(articleId);
    Map<String, Object> response = new HashMap<>();
    response.put("comments", comments);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable String articleId,
      @PathVariable String commentId,
      @RequestHeader("X-User-Id") String userId) {
    boolean deleted = commentService.deleteComment(commentId, userId);
    if (deleted) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  @Getter
  @NoArgsConstructor
  static class NewCommentRequest {
    @NotBlank(message = "can't be empty")
    private String body;
  }
}
