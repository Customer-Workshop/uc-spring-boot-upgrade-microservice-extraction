package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  @Test
  public void should_handle_next_direction_with_extra() {
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "a1", new DateTime(), new DateTime(), profile);
    CursorPager<CommentData> pager =
        new CursorPager<>(Collections.singletonList(c1), Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
  }

  @Test
  public void should_handle_prev_direction_with_extra() {
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "a1", new DateTime(), new DateTime(), profile);
    CursorPager<CommentData> pager =
        new CursorPager<>(Collections.singletonList(c1), Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  public void should_return_null_cursors_when_empty() {
    CursorPager<CommentData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_return_correct_cursors_for_multiple_items() {
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "a1", new DateTime(2020, 1, 1, 0, 0), new DateTime(), profile);
    CommentData c2 = new CommentData("c2", "body2", "a1", new DateTime(2020, 2, 1, 0, 0), new DateTime(), profile);
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(c1, c2), Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
    assertNotEquals(pager.getStartCursor().toString(), pager.getEndCursor().toString());
  }
}
