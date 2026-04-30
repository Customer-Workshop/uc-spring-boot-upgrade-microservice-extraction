package io.spring.comments.controller;

import io.spring.comments.dto.CommentRequest;
import io.spring.comments.dto.CommentResponse;
import io.spring.comments.model.Comment;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
public class CommentController {

  private final CommentRepository commentRepository;

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getArticleId(), request.getUserId());
    commentRepository.save(comment);
    return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.fromEntity(comment));
  }

  @GetMapping
  public ResponseEntity<List<CommentResponse>> getCommentsByArticle(
      @RequestParam("articleId") String articleId) {
    List<CommentResponse> comments =
        commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId).stream()
            .map(CommentResponse::fromEntity)
            .collect(Collectors.toList());
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentResponse> getComment(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = false) String articleId) {
    Comment comment;
    if (articleId != null) {
      comment =
          commentRepository
              .findByIdAndArticleId(id, articleId)
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    } else {
      comment =
          commentRepository
              .findById(id)
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }
    return ResponseEntity.ok(CommentResponse.fromEntity(comment));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    if (!commentRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found");
    }
    commentRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
