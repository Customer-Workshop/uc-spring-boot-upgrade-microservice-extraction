package io.spring.comments.api;

import io.spring.comments.domain.Comment;
import io.spring.comments.repository.CommentMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
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
@RequestMapping(path = "/api/comments")
public class CommentsController {

  private final CommentMapper commentMapper;

  public CommentsController(CommentMapper commentMapper) {
    this.commentMapper = commentMapper;
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    commentMapper.insert(comment);
    Map<String, Object> response = new HashMap<>();
    response.put("comment", toResponse(comment));
    return ResponseEntity.status(201).body(response);
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> getCommentsByArticleId(
      @RequestParam("articleId") String articleId) {
    List<Comment> comments = commentMapper.findByArticleId(articleId);
    List<CommentResponse> responses =
        comments.stream().map(this::toResponse).collect(Collectors.toList());
    Map<String, Object> response = new HashMap<>();
    response.put("comments", responses);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Map<String, Object>> getCommentById(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = false) String articleId) {
    Comment comment;
    if (articleId != null) {
      comment = commentMapper.findByIdAndArticleId(articleId, id);
    } else {
      comment = commentMapper.findById(id);
    }
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    Map<String, Object> response = new HashMap<>();
    response.put("comment", toResponse(comment));
    return ResponseEntity.ok(response);
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

  private CommentResponse toResponse(Comment comment) {
    return new CommentResponse(
        comment.getId(),
        comment.getBody(),
        comment.getUserId(),
        comment.getArticleId(),
        comment.getCreatedAt().toString());
  }
}
