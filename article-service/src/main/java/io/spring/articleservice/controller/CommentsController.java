package io.spring.articleservice.controller;

import io.spring.articleservice.dto.CommentDto;
import io.spring.articleservice.dto.request.CreateCommentWrapper;
import io.spring.articleservice.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles/{slug}/comments")
@RequiredArgsConstructor
public class CommentsController {

  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<Map<String, CommentDto>> createComment(
      @PathVariable String slug,
      @AuthenticationPrincipal String userId,
      @Valid @RequestBody CreateCommentWrapper wrapper) {
    CommentDto comment = commentService.createComment(slug, wrapper.comment().body(), userId);
    return ResponseEntity.status(201).body(Map.of("comment", comment));
  }

  @GetMapping
  public ResponseEntity<Map<String, List<CommentDto>>> getComments(
      @PathVariable String slug, @AuthenticationPrincipal String userId) {
    List<CommentDto> comments = commentService.getCommentsBySlug(slug, userId);
    return ResponseEntity.ok(Map.of("comments", comments));
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable String slug,
      @PathVariable String commentId,
      @AuthenticationPrincipal String userId) {
    commentService.deleteComment(slug, commentId, userId);
    return ResponseEntity.noContent().build();
  }
}
