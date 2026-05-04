package io.spring.comments.api;

import io.spring.comments.api.dto.CommentResponse;
import io.spring.comments.api.dto.CreateCommentRequest;
import io.spring.comments.domain.Comment;
import io.spring.comments.repository.CommentMapper;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.joda.time.format.ISODateTimeFormat;
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

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
public class CommentsController {

  private final CommentMapper commentMapper;

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    commentMapper.insert(comment);
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(comment));
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentResponse> getComment(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = false) String articleId) {
    Comment comment;
    if (articleId != null) {
      comment = commentMapper.findByIdAndArticleId(id, articleId);
    } else {
      comment = commentMapper.findById(id);
    }
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(toResponse(comment));
  }

  @GetMapping
  public ResponseEntity<List<CommentResponse>> getCommentsByArticle(
      @RequestParam("articleId") String articleId) {
    List<Comment> comments = commentMapper.findByArticleId(articleId);
    List<CommentResponse> responses =
        comments.stream().map(this::toResponse).collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    Comment comment = commentMapper.findById(id);
    if (comment == null) {
      return ResponseEntity.notFound().build();
    }
    commentMapper.delete(id);
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
