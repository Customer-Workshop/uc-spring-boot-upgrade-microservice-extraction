package io.spring.comments.api;

import io.spring.comments.application.CommentQueryService;
import io.spring.comments.application.CursorPageParameter;
import io.spring.comments.application.CursorPager;
import io.spring.comments.application.CursorPager.Direction;
import io.spring.comments.application.DateTimeCursor;
import io.spring.comments.application.PageCursor;
import io.spring.comments.application.data.CommentData;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Cursor-paginated comment reads consumed by the monolith's GraphQL comment connection. */
@RestController
@RequestMapping(path = "/internal/comments")
@AllArgsConstructor
public class InternalCommentsApi {
  private CommentQueryService commentQueryService;

  @GetMapping
  public ResponseEntity<?> getComments(
      @RequestParam("articleId") String articleId,
      @RequestParam(value = "cursor", required = false) String cursor,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "direction", defaultValue = "NEXT") Direction direction) {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(DateTimeCursor.parse(cursor), limit, direction);
    CursorPager<CommentData> pager =
        commentQueryService.findByArticleIdWithCursor(articleId, null, page);
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("comments", pager.getData());
    response.put("hasNext", pager.hasNext());
    response.put("hasPrevious", pager.hasPrevious());
    response.put("startCursor", cursorValue(pager.getStartCursor()));
    response.put("endCursor", cursorValue(pager.getEndCursor()));
    return ResponseEntity.ok(response);
  }

  private String cursorValue(PageCursor cursor) {
    return cursor == null ? null : cursor.toString();
  }
}
