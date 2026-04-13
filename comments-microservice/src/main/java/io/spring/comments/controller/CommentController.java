package io.spring.comments.controller;

import io.spring.comments.model.Comment;
import io.spring.comments.repository.CommentMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
public class CommentController {

  private CommentMapper commentMapper;

  @PostMapping
  public ResponseEntity<Map<String, Object>> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment;
    if (request.getId() != null && !request.getId().isEmpty()) {
      comment =
          new Comment(
              request.getId(), request.getBody(), request.getUserId(), request.getArticleId());
    } else {
      comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    }
    commentMapper.insert(comment);
    Comment saved = commentMapper.findByIdOnly(comment.getId());
    Map<String, Object> response = new HashMap<>();
    response.put("comment", toMap(saved));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/article/{articleId}")
  public ResponseEntity<Map<String, Object>> getCommentsByArticle(@PathVariable String articleId) {
    List<Comment> comments = commentMapper.findByArticleId(articleId);
    List<Map<String, Object>> commentMaps =
        comments.stream().map(this::toMap).collect(java.util.stream.Collectors.toList());
    Map<String, Object> response = new HashMap<>();
    response.put("comments", commentMaps);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getCommentById(@PathVariable String id) {
    Comment comment = commentMapper.findByIdOnly(id);
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    Map<String, Object> response = new HashMap<>();
    response.put("comment", toMap(comment));
    return ResponseEntity.ok(response);
  }

  @GetMapping("/article/{articleId}/{id}")
  public ResponseEntity<Map<String, Object>> getCommentByArticleAndId(
      @PathVariable String articleId, @PathVariable String id) {
    Comment comment = commentMapper.findById(articleId, id);
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    Map<String, Object> response = new HashMap<>();
    response.put("comment", toMap(comment));
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable String id) {
    Comment comment = commentMapper.findByIdOnly(id);
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    commentMapper.delete(id);
    return ResponseEntity.noContent().build();
  }

  private Map<String, Object> toMap(Comment comment) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", comment.getId());
    map.put("body", comment.getBody());
    map.put("userId", comment.getUserId());
    map.put("articleId", comment.getArticleId());
    map.put("createdAt", comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null);
    return map;
  }
}

@Getter
@Setter
@NoArgsConstructor
class CreateCommentRequest {
  private String id;

  @NotBlank(message = "can't be empty")
  private String body;

  @NotBlank(message = "can't be empty")
  private String userId;

  @NotBlank(message = "can't be empty")
  private String articleId;
}
