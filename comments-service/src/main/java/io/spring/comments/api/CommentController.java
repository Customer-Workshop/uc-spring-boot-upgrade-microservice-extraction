package io.spring.comments.api;

import io.spring.comments.application.CommentService;
import io.spring.comments.application.data.CommentData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
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
@RequestMapping(path = "/comments")
public class CommentController {
  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody NewCommentParam param) {
    DateTime createdAt =
        param.getCreatedAt() == null
            ? null
            : ISODateTimeFormat.dateTimeParser().parseDateTime(param.getCreatedAt());
    CommentData data =
        commentService.create(
            param.getId(), param.getBody(), param.getUserId(), param.getArticleId(), createdAt);
    return ResponseEntity.status(201).body(commentResponse(data));
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> findByArticle(
      @RequestParam("articleId") String articleId,
      @RequestParam(value = "viewerId", required = false) String viewerId,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "limit", required = false) Integer limit,
      @RequestParam(value = "direction", required = false) String direction) {
    List<CommentData> comments;
    if (limit != null) {
      DateTime cursorValue =
          cursor == null ? null : ISODateTimeFormat.dateTimeParser().parseDateTime(cursor);
      boolean next = direction == null || !"PREV".equalsIgnoreCase(direction);
      comments =
          commentService.findByArticleIdWithCursor(articleId, viewerId, cursorValue, limit, next);
    } else {
      comments = commentService.findByArticleId(articleId, viewerId);
    }
    Map<String, Object> response = new HashMap<>();
    response.put("comments", comments);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> findById(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = false) String articleId,
      @RequestParam(value = "viewerId", required = false) String viewerId) {
    return commentService
        .findById(id, articleId, viewerId)
        .map(data -> ResponseEntity.ok(commentResponse(data)))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") String id) {
    if (commentService.delete(id)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  private Map<String, Object> commentResponse(CommentData data) {
    Map<String, Object> response = new HashMap<>();
    response.put("comment", data);
    return response;
  }
}
