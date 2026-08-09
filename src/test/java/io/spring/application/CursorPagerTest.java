package io.spring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.application.data.CommentData;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class CursorPagerTest {
  private final CommentData comment =
      new CommentData("id", "body", "article", new DateTime(), new DateTime(), null);

  @Test
  void tracksNextAndPreviousFlagsAndCursors() {
    CursorPager<CommentData> next =
        new CursorPager<>(Collections.singletonList(comment), CursorPager.Direction.NEXT, true);
    assertTrue(next.hasNext());
    assertFalse(next.hasPrevious());
    assertEquals(comment.getCursor().toString(), next.getStartCursor().toString());
    assertEquals(comment.getCursor().toString(), next.getEndCursor().toString());

    CursorPager<CommentData> previous =
        new CursorPager<>(Arrays.asList(comment), CursorPager.Direction.PREV, true);
    assertFalse(previous.hasNext());
    assertTrue(previous.hasPrevious());

    CursorPager<CommentData> empty =
        new CursorPager<>(Collections.emptyList(), CursorPager.Direction.NEXT, false);
    assertNull(empty.getStartCursor());
    assertNull(empty.getEndCursor());
  }
}
