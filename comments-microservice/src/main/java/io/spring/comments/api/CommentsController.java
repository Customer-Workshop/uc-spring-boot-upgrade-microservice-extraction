package io.spring.comments.api;

import io.spring.comments.core.Comment;
import io.spring.comments.core.CommentRepository;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
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
public class CommentsController {

  private final CommentRepository commentRepository;

  public CommentsController(CommentRepository commentRepository) {
    this.commentRepository = commentRepository;
  }

  @PostMapping
  public ResponseEntity<CommentDto> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    commentRepository.save(comment);
    return ResponseEntity.status(201).body(toDto(comment));
  }

  @GetMapping
  public ResponseEntity<List<CommentDto>> getCommentsByArticle(
      @RequestParam("articleId") String articleId) {
    List<Comment> comments = commentRepository.findByArticleId(articleId);
    List<CommentDto> dtos = comments.stream().map(this::toDto).collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentDto> getComment(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = false) String articleId) {
    if (articleId != null) {
      return commentRepository
          .findById(articleId, id)
          .map(c -> ResponseEntity.ok(toDto(c)))
          .orElse(ResponseEntity.notFound().build());
    }
    return commentRepository
        .findById(id)
        .map(c -> ResponseEntity.ok(toDto(c)))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    return commentRepository
        .findById(id)
        .map(
            comment -> {
              commentRepository.remove(comment);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  private CommentDto toDto(Comment comment) {
    String createdAt =
        comment.getCreatedAt() != null
            ? ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt())
            : null;
    return new CommentDto(
        comment.getId(),
        comment.getBody(),
        comment.getUserId(),
        comment.getArticleId(),
        createdAt,
        createdAt);
  }
}
