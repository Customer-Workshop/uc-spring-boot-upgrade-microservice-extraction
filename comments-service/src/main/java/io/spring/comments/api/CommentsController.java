package io.spring.comments.api;

import io.spring.comments.core.Comment;
import io.spring.comments.infrastructure.CommentMapper;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.joda.time.format.ISODateTimeFormat;
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
public class CommentsController {

  private CommentMapper commentMapper;

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
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
    return ResponseEntity.status(201).body(toResponse(comment));
  }

  @GetMapping("/article/{articleId}")
  public ResponseEntity<List<CommentResponse>> getCommentsByArticleId(
      @PathVariable String articleId) {
    List<Comment> comments = commentMapper.findByArticleId(articleId);
    List<CommentResponse> responses =
        comments.stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{commentId}/article/{articleId}")
  public ResponseEntity<CommentResponse> getComment(
      @PathVariable String commentId, @PathVariable String articleId) {
    Comment comment = commentMapper.findById(articleId, commentId);
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(toResponse(comment));
  }

  @GetMapping("/by-id/{commentId}")
  public ResponseEntity<CommentResponse> getCommentById(@PathVariable String commentId) {
    Comment comment = commentMapper.findByIdOnly(commentId);
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(toResponse(comment));
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
    commentMapper.delete(commentId);
    return ResponseEntity.noContent().build();
  }

  private CommentResponse toResponse(Comment comment) {
    return new CommentResponse(
        comment.getId(),
        comment.getBody(),
        comment.getUserId(),
        comment.getArticleId(),
        ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt()));
  }
}
