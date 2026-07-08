package io.spring.comments.api;

import io.spring.comments.api.dto.NewCommentRequest;
import io.spring.comments.application.CommentQueryService;
import io.spring.comments.application.data.CommentData;
import io.spring.comments.core.comment.Comment;
import io.spring.comments.core.comment.CommentRepository;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
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

/**
 * REST API for the Comments bounded context. Comments are keyed only by {@code articleId} and
 * {@code userId}; the monolith owns article/user identity and authorization.
 */
@RestController
@RequestMapping(path = "/comments")
@AllArgsConstructor
public class CommentController {

  private final CommentRepository commentRepository;
  private final CommentQueryService commentQueryService;

  @PostMapping
  public ResponseEntity<CommentData> createComment(
      @Valid @RequestBody NewCommentRequest request,
      @RequestParam(value = "viewerId", required = false) String viewerId) {
    Comment comment =
        (request.getId() == null || request.getId().isEmpty())
            ? new Comment(request.getBody(), request.getUserId(), request.getArticleId())
            : new Comment(
                request.getId(),
                request.getBody(),
                request.getUserId(),
                request.getArticleId(),
                new DateTime());
    commentRepository.save(comment);
    CommentData data =
        commentQueryService
            .findById(comment.getId(), viewerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    return ResponseEntity.status(HttpStatus.CREATED).body(data);
  }

  @GetMapping
  public List<CommentData> getComments(
      @RequestParam("articleId") String articleId,
      @RequestParam(value = "viewerId", required = false) String viewerId) {
    return commentQueryService.findByArticleId(articleId, viewerId);
  }

  @GetMapping(path = "/cursor")
  public List<CommentData> getCommentsWithCursor(
      @RequestParam("articleId") String articleId,
      @RequestParam(value = "viewerId", required = false) String viewerId,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "direction", defaultValue = "NEXT") String direction) {
    DateTime cursorTime = (cursor == null || cursor.isEmpty()) ? null : DateTime.parse(cursor);
    return commentQueryService.findByArticleIdWithCursor(
        articleId, viewerId, cursorTime, direction);
  }

  @GetMapping(path = "/{id}")
  public CommentData getComment(
      @PathVariable("id") String id,
      @RequestParam(value = "viewerId", required = false) String viewerId) {
    return commentQueryService
        .findById(id, viewerId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @DeleteMapping(path = "/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    return commentRepository
        .findById(id)
        .map(
            comment -> {
              commentRepository.remove(comment);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }
}
