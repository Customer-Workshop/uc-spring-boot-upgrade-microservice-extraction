package io.spring.commentservice.api;

import io.spring.commentservice.application.CommentService;
import io.spring.commentservice.application.dto.CommentData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/comments")
public class InternalCommentsController {
  private final CommentService commentService;

  public InternalCommentsController(CommentService commentService) {
    this.commentService = commentService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> findById(@PathVariable("id") String id) {
    return commentService
        .findById(id)
        .map(
            comment -> {
              Map<String, Object> response = new HashMap<>();
              response.put("comment", comment);
              return ResponseEntity.ok(response);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<?> findByArticleId(@RequestParam("articleId") String articleId) {
    List<CommentData> comments = commentService.findByArticleId(articleId);
    Map<String, Object> response = new HashMap<>();
    response.put("comments", comments);
    return ResponseEntity.ok(response);
  }
}
