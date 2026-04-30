package io.spring.comments.api;

import io.spring.comments.domain.Comment;
import io.spring.comments.repository.CommentRepository;
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
    List<CommentDto> comments =
        commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentDto> getComment(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = false) String articleId) {
    if (articleId != null && !articleId.isEmpty()) {
      return commentRepository
          .findByIdAndArticleId(id, articleId)
          .map(comment -> ResponseEntity.ok(toDto(comment)))
          .orElse(ResponseEntity.notFound().build());
    }
    return commentRepository
        .findById(id)
        .map(comment -> ResponseEntity.ok(toDto(comment)))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    if (commentRepository.existsById(id)) {
      commentRepository.deleteById(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
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
