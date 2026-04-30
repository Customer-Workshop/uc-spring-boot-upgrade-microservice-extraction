package io.spring.comments.api;

import io.spring.comments.domain.Comment;
import io.spring.comments.domain.CommentRepository;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
public class CommentsController {
  private final CommentRepository commentRepository;

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    commentRepository.save(comment);
    return ResponseEntity.status(201).body(toResponse(comment));
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentResponse> getComment(@PathVariable String id) {
    return commentRepository
        .findById(id)
        .map(comment -> ResponseEntity.ok(toResponse(comment)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<List<CommentResponse>> getCommentsByArticle(
      @RequestParam String articleId) {
    List<Comment> comments = commentRepository.findByArticleId(articleId);
    List<CommentResponse> responses =
        comments.stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/by-article-and-id")
  public ResponseEntity<CommentResponse> getCommentByArticleAndId(
      @RequestParam String articleId, @RequestParam String id) {
    return commentRepository
        .findByIdAndArticleId(id, articleId)
        .map(comment -> ResponseEntity.ok(toResponse(comment)))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable String id) {
    if (commentRepository.findById(id).isPresent()) {
      commentRepository.remove(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }

  private CommentResponse toResponse(Comment comment) {
    String createdAt = ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt());
    return CommentResponse.builder()
        .id(comment.getId())
        .body(comment.getBody())
        .articleId(comment.getArticleId())
        .userId(comment.getUserId())
        .createdAt(createdAt)
        .updatedAt(createdAt)
        .build();
  }
}
