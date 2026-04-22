package io.spring.comments.api;

import io.spring.comments.core.Comment;
import io.spring.comments.core.CommentRepository;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class CommentsController {

  private CommentRepository commentRepository;

  @PostMapping
  public ResponseEntity<CommentDto> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    commentRepository.save(comment);
    return ResponseEntity.status(201).body(toDto(comment));
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentDto> getComment(@PathVariable("id") String id) {
    return commentRepository
        .findById(id)
        .map(comment -> ResponseEntity.ok(toDto(comment)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<List<CommentDto>> getCommentsByArticleId(
      @RequestParam("articleId") String articleId) {
    List<CommentDto> comments =
        commentRepository.findByArticleId(articleId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/by-article/{articleId}/{commentId}")
  public ResponseEntity<CommentDto> getCommentByArticleAndId(
      @PathVariable("articleId") String articleId,
      @PathVariable("commentId") String commentId) {
    return commentRepository
        .findByArticleIdAndId(articleId, commentId)
        .map(comment -> ResponseEntity.ok(toDto(comment)))
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
    return new CommentDto(
        comment.getId(),
        comment.getBody(),
        comment.getUserId(),
        comment.getArticleId(),
        comment.getCreatedAt(),
        comment.getUpdatedAt());
  }
}
