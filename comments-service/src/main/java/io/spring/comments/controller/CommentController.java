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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/comments")
@AllArgsConstructor
public class CommentController {
  private final CommentService commentService;

  @PostMapping("/articles/{articleId}")
  public ResponseEntity<Map<String, Object>> createComment(
      @PathVariable("articleId") String articleId,
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment =
        commentService.createComment(request.getBody(), request.getUserId(), articleId);
    return ResponseEntity.status(201).body(wrapComment(comment));
  }

  @GetMapping("/articles/{articleId}")
  public ResponseEntity<Map<String, Object>> getCommentsByArticleId(
      @PathVariable("articleId") String articleId) {
    List<Comment> comments = commentService.findByArticleId(articleId);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getCommentById(@PathVariable("id") String id) {
    return commentService
        .findById(id)
        .map(comment -> ResponseEntity.ok(wrapComment(comment)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/articles/{articleId}/{id}")
  public ResponseEntity<Map<String, Object>> getCommentByArticleIdAndId(
      @PathVariable("articleId") String articleId, @PathVariable("id") String id) {
    return commentService
        .findByArticleIdAndId(articleId, id)
        .map(comment -> ResponseEntity.ok(wrapComment(comment)))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    if (commentService.findById(id).isPresent()) {
      commentService.deleteComment(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  private Map<String, Object> wrapComment(Comment comment) {
    return new HashMap<String, Object>() {
      {
        put("comment", comment);
      }
    };
  }
}

@Getter
@NoArgsConstructor
class CreateCommentRequest {
  @NotBlank(message = "can't be empty")
  private String body;

  @NotBlank(message = "can't be empty")
  private String userId;
}
