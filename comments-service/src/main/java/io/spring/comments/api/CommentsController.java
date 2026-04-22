package io.spring.comments.api;

import io.spring.comments.core.Comment;
import io.spring.comments.core.CommentRepository;
import io.spring.comments.infrastructure.mybatis.mapper.CommentMapper;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentsController {

  private final CommentRepository commentRepository;
  private final CommentMapper commentMapper;

  public CommentsController(CommentRepository commentRepository, CommentMapper commentMapper) {
    this.commentRepository = commentRepository;
    this.commentMapper = commentMapper;
  }

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    commentRepository.save(comment);
    return ResponseEntity.status(201).body(toResponse(comment));
  }

  @GetMapping("/article/{articleId}")
  public ResponseEntity<List<CommentResponse>> getCommentsByArticle(
      @PathVariable String articleId) {
    List<Comment> comments = commentMapper.findByArticleId(articleId);
    List<CommentResponse> responses =
        comments.stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{commentId}/article/{articleId}")
  public ResponseEntity<CommentResponse> getComment(
      @PathVariable String commentId, @PathVariable String articleId) {
    return commentRepository
        .findById(articleId, commentId)
        .map(comment -> ResponseEntity.ok(toResponse(comment)))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{commentId}/article/{articleId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable String commentId, @PathVariable String articleId) {
    return commentRepository
        .findById(articleId, commentId)
        .map(
            comment -> {
              commentRepository.remove(comment);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElse(ResponseEntity.notFound().build());
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
