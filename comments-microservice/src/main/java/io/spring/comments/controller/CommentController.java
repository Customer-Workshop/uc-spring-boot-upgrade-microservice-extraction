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
import lombok.Setter;
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
  private CommentService commentService;

  @PostMapping
  public ResponseEntity<Map<String, Object>> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment =
        commentService.createComment(request.getBody(), request.getUserId(), request.getArticleId());
    return ResponseEntity.status(201).body(commentResponse(comment));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getComment(@PathVariable("id") String id) {
    return commentService
        .findById(id)
        .map(comment -> ResponseEntity.ok(commentResponse(comment)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/article/{articleId}")
  public ResponseEntity<Map<String, Object>> getCommentsByArticle(
      @PathVariable("articleId") String articleId) {
    List<Comment> comments = commentService.findByArticleId(articleId);
    Map<String, Object> response = new HashMap<>();
    response.put("comments", comments);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/article/{articleId}/{id}")
  public ResponseEntity<Map<String, Object>> getCommentByArticleAndId(
      @PathVariable("articleId") String articleId, @PathVariable("id") String id) {
    return commentService
        .findByArticleIdAndId(articleId, id)
        .map(comment -> ResponseEntity.ok(commentResponse(comment)))
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

  private Map<String, Object> commentResponse(Comment comment) {
    Map<String, Object> response = new HashMap<>();
    response.put("comment", comment);
    return response;
  }
}

@Getter
@Setter
@NoArgsConstructor
class CreateCommentRequest {
  @NotBlank(message = "can't be empty")
  private String body;

  @NotBlank(message = "can't be empty")
  private String userId;

  @NotBlank(message = "can't be empty")
  private String articleId;
}
