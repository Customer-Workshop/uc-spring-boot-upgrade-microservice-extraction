package io.spring.comments.controller;

import io.spring.comments.model.Comment;
import io.spring.comments.repository.CommentMapper;
import java.util.List;
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
@RequestMapping("/api/comments")
public class CommentsController {

  private final CommentMapper commentMapper;

  public CommentsController(CommentMapper commentMapper) {
    this.commentMapper = commentMapper;
  }

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @Valid @RequestBody CommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    commentMapper.insert(comment);
    return ResponseEntity.status(201).body(toResponse(comment));
  }

  @GetMapping
  public ResponseEntity<List<CommentResponse>> getCommentsByArticleId(
      @RequestParam("articleId") String articleId) {
    List<Comment> comments = commentMapper.findByArticleId(articleId);
    List<CommentResponse> responses =
        comments.stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentResponse> getCommentById(
      @PathVariable("id") String id, @RequestParam("articleId") String articleId) {
    Comment comment = commentMapper.findById(articleId, id);
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(toResponse(comment));
  }

  @GetMapping("/by-id/{id}")
  public ResponseEntity<CommentResponse> getCommentByIdOnly(@PathVariable("id") String id) {
    Comment comment = commentMapper.findByIdOnly(id);
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(toResponse(comment));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    commentMapper.delete(id);
    return ResponseEntity.noContent().build();
  }

  private CommentResponse toResponse(Comment comment) {
    return new CommentResponse(
        comment.getId(),
        comment.getBody(),
        comment.getUserId(),
        comment.getArticleId(),
        comment.getCreatedAt().toString(),
        comment.getUpdatedAt() != null ? comment.getUpdatedAt().toString() : comment.getCreatedAt().toString());
  }
}
