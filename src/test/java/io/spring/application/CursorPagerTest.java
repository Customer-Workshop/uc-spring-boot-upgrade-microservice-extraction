package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import java.util.ArrayList;
import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  @Test
  void should_have_next_when_direction_is_next_and_has_extra() {
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    CommentData data =
        new CommentData("c1", "body", "article1", new DateTime(), new DateTime(), profile);
    CursorPager<CommentData> pager = new CursorPager<>(Arrays.asList(data), Direction.NEXT, true);
    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_have_previous_when_direction_is_prev_and_has_extra() {
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    CommentData data =
        new CommentData("c1", "body", "article1", new DateTime(), new DateTime(), profile);
    CursorPager<CommentData> pager = new CursorPager<>(Arrays.asList(data), Direction.PREV, true);
    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void should_return_null_cursors_when_empty() {
    CursorPager<CommentData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  void should_return_cursors_when_data_exists() {
    ProfileData profile = new ProfileData("id", "user", "bio", "img", false);
    CommentData data =
        new CommentData("c1", "body", "article1", new DateTime(), new DateTime(), profile);
    CursorPager<CommentData> pager = new CursorPager<>(Arrays.asList(data), Direction.NEXT, false);
    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
  }
}
