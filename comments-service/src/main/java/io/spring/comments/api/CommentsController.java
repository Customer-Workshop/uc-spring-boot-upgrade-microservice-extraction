package io.spring.comments.api;

import io.spring.comments.api.exception.ResourceNotFoundException;
import io.spring.comments.application.CommentQueryService;
import io.spring.comments.application.CursorPageParameter;
import io.spring.comments.application.CursorPager;
import io.spring.comments.application.DateTimeCursor;
import io.spring.comments.application.data.CommentData;
import io.spring.comments.core.comment.Comment;
import io.spring.comments.core.comment.CommentRepository;
import java.util.List;
import javax.validation.Valid;
import org.joda.time.DateTime;
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
@RequestMapping(path = "/comments")
public class CommentsController {

  private final CommentRepository commentRepository;
  private final CommentQueryService commentQueryService;

  public CommentsController(
      CommentRepository commentRepository, CommentQueryService commentQueryService) {
    this.commentRepository = commentRepository;
    this.commentQueryService = commentQueryService;
  }

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @Valid @RequestBody NewCommentRequest request) {
    Comment comment =
        new Comment(
            request.getId(),
            request.getBody(),
            request.getUserId(),
            request.getArticleId(),
            parseDateTime(request.getCreatedAt()));
    commentRepository.save(comment);
    return ResponseEntity.status(201).body(CommentResponse.from(comment));
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentResponse> getRawComment(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = false) String articleId) {
    Comment comment =
        (articleId == null
                ? commentRepository.findById(id)
                : commentRepository.findById(articleId, id))
            .orElseThrow(ResourceNotFoundException::new);
    return ResponseEntity.ok(CommentResponse.from(comment));
  }

  @GetMapping("/{id}/data")
  public ResponseEntity<CommentData> getCommentData(
      @PathVariable("id") String id,
      @RequestParam(value = "viewerId", required = false) String viewerId) {
    CommentData data =
        commentQueryService.findById(id, viewerId).orElseThrow(ResourceNotFoundException::new);
    return ResponseEntity.ok(data);
  }

  @GetMapping
  public ResponseEntity<List<CommentData>> getCommentsByArticle(
      @RequestParam("articleId") String articleId,
      @RequestParam(value = "viewerId", required = false) String viewerId) {
    return ResponseEntity.ok(commentQueryService.findByArticleId(articleId, viewerId));
  }

  @GetMapping("/cursor")
  public ResponseEntity<CursorResponse> getCommentsByArticleWithCursor(
      @RequestParam("articleId") String articleId,
      @RequestParam(value = "viewerId", required = false) String viewerId,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "direction", defaultValue = "NEXT") CursorPager.Direction direction) {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(DateTimeCursor.parse(cursor), limit, direction);
    CursorPager<CommentData> pager =
        commentQueryService.findByArticleIdWithCursor(articleId, viewerId, page);
    CursorResponse response =
        new CursorResponse(
            pager.getData(),
            pager.hasNext(),
            pager.hasPrevious(),
            pager.getStartCursor() == null ? null : pager.getStartCursor().toString(),
            pager.getEndCursor() == null ? null : pager.getEndCursor().toString());
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(@PathVariable("id") String id) {
    commentRepository.findById(id).ifPresent(commentRepository::remove);
    return ResponseEntity.noContent().build();
  }

  private DateTime parseDateTime(String value) {
    if (value == null || value.isEmpty()) {
      return null;
    }
    return org.joda.time.format.ISODateTimeFormat.dateTimeParser()
        .withZoneUTC()
        .parseDateTime(value);
  }
}
