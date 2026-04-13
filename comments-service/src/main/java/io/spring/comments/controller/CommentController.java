package io.spring.comments.controller;

import io.spring.comments.mapper.CommentMapper;
import io.spring.comments.model.Comment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

  private CommentMapper commentMapper;

  @PostMapping
  public ResponseEntity<Comment> createComment(@Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    commentMapper.insert(comment);
    Comment saved = commentMapper.findById(comment.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @GetMapping
  public ResponseEntity<List<Comment>> getCommentsByArticleId(
      @RequestParam("articleId") String articleId) {
    List<Comment> comments = commentMapper.findByArticleId(articleId);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Comment> getCommentById(@PathVariable("id") String id) {
    Comment comment = commentMapper.findById(id);
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(comment);
  }

  @GetMapping("/{id}/by-article")
  public ResponseEntity<Comment> getCommentByIdAndArticleId(
      @PathVariable("id") String id, @RequestParam("articleId") String articleId) {
    Comment comment = commentMapper.findByIdAndArticleId(id, articleId);
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(comment);
  }

  @GetMapping("/cursor")
  public ResponseEntity<Map<String, Object>> getCommentsByArticleIdWithCursor(
      @RequestParam("articleId") String articleId,
      @RequestParam(value = "cursor", required = false) Long cursor,
      @RequestParam(value = "direction", defaultValue = "NEXT") String direction,
      @RequestParam(value = "limit", defaultValue = "20") int limit) {
    DateTime cursorDateTime = cursor != null
        ? new DateTime(cursor, DateTimeZone.UTC)
        : null;
    int queryLimit = limit + 1;
    List<Comment> comments =
        commentMapper.findByArticleIdWithCursor(articleId, cursorDateTime, direction, queryLimit);
    boolean hasExtra = comments.size() > limit;
    if (hasExtra) {
      comments = comments.subList(0, limit);
    }
    Map<String, Object> result = new HashMap<>();
    result.put("comments", comments);
    result.put("hasExtra", hasExtra);
    return ResponseEntity.ok(result);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    Comment comment = commentMapper.findById(id);
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    commentMapper.delete(id);
    return ResponseEntity.noContent().build();
  }
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CreateCommentRequest {
  @NotBlank(message = "can't be empty")
  private String body;

  @NotBlank(message = "can't be empty")
  private String userId;

  @NotBlank(message = "can't be empty")
  private String articleId;
}
