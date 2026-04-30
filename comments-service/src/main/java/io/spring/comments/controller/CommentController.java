package io.spring.comments.controller;

import io.spring.comments.domain.Comment;
import io.spring.comments.dto.CommentDto;
import io.spring.comments.dto.CreateCommentRequest;
import io.spring.comments.repository.CommentRepository;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
public class CommentController {

  private final CommentRepository commentRepository;

  @PostMapping
  public ResponseEntity<CommentDto> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    commentRepository.save(comment);
    return ResponseEntity.status(HttpStatus.CREATED).body(CommentDto.fromEntity(comment));
  }

  @GetMapping("/article/{articleId}")
  public ResponseEntity<List<CommentDto>> getCommentsByArticle(@PathVariable String articleId) {
    List<CommentDto> comments =
        commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId).stream()
            .map(CommentDto::fromEntity)
            .collect(Collectors.toList());
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentDto> getCommentById(@PathVariable String id) {
    return commentRepository
        .findById(id)
        .map(comment -> ResponseEntity.ok(CommentDto.fromEntity(comment)))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
  }

  @GetMapping("/{id}/article/{articleId}")
  public ResponseEntity<CommentDto> getCommentByIdAndArticle(
      @PathVariable String id, @PathVariable String articleId) {
    return commentRepository
        .findByIdAndArticleId(id, articleId)
        .map(comment -> ResponseEntity.ok(CommentDto.fromEntity(comment)))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable String id) {
    if (!commentRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
    }
    commentRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
