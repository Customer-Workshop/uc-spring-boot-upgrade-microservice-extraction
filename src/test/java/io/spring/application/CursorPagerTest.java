package io.spring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  private CommentData commentAt(String id, DateTime createdAt) {
    return new CommentData(
        id,
        "body",
        "article-id",
        createdAt,
        createdAt,
        new ProfileData("user-id", "username", "bio", "image", false));
  }

  @Test
  public void should_expose_next_page_for_forward_direction() {
    List<CommentData> data =
        Arrays.asList(commentAt("1", new DateTime(1000L)), commentAt("2", new DateTime(2000L)));

    CursorPager<CommentData> pager = new CursorPager<>(data, Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
    assertEquals("1000", pager.getStartCursor().toString());
    assertEquals("2000", pager.getEndCursor().toString());
    assertEquals(data, pager.getData());
  }

  @Test
  public void should_expose_previous_page_for_backward_direction() {
    CursorPager<CommentData> pager =
        new CursorPager<>(
            Collections.singletonList(commentAt("1", new DateTime(1000L))), Direction.PREV, true);

    assertTrue(pager.hasPrevious());
    assertFalse(pager.hasNext());
  }

  @Test
  public void should_return_null_cursors_for_empty_page() {
    CursorPager<CommentData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
    assertFalse(pager.hasNext());
  }

  @Test
  public void should_cap_and_default_page_limit() {
    assertEquals(1000, new CursorPageParameter<>(null, 5000, Direction.NEXT).getLimit());
    assertEquals(20, new CursorPageParameter<>(null, 0, Direction.NEXT).getLimit());
    assertEquals(11, new CursorPageParameter<>(null, 10, Direction.NEXT).getQueryLimit());
    assertTrue(new CursorPageParameter<>(null, 10, Direction.NEXT).isNext());
    assertFalse(new CursorPageParameter<>(null, 10, Direction.PREV).isNext());
  }

  @Test
  public void should_parse_and_render_date_time_cursor() {
    assertNull(DateTimeCursor.parse(null));
    assertEquals(1000L, DateTimeCursor.parse("1000").getMillis());
    assertEquals("1000", new DateTimeCursor(new DateTime(1000L)).toString());
  }
}
