package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.util.ArrayList;
import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  private ArticleData createArticleData(String id, DateTime time) {
    return new ArticleData(
        id,
        "slug-" + id,
        "title",
        "desc",
        "body",
        false,
        0,
        time,
        time,
        null,
        new ProfileData("user-id", "username", "bio", "image", false));
  }

  @Test
  void should_have_next_when_direction_next_and_has_extra() {
    DateTime now = new DateTime();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(createArticleData("1", now)), Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_not_have_next_when_direction_next_and_no_extra() {
    DateTime now = new DateTime();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(createArticleData("1", now)), Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_have_previous_when_direction_prev_and_has_extra() {
    DateTime now = new DateTime();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(createArticleData("1", now)), Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void should_not_have_previous_when_direction_prev_and_no_extra() {
    DateTime now = new DateTime();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(createArticleData("1", now)), Direction.PREV, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_get_start_cursor() {
    DateTime now = new DateTime();
    ArticleData data = createArticleData("1", now);
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(data), Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertEquals(now.getMillis(), ((DateTimeCursor) pager.getStartCursor()).getData().getMillis());
  }

  @Test
  void should_get_end_cursor() {
    DateTime time1 = new DateTime().minusHours(1);
    DateTime time2 = new DateTime();
    CursorPager<ArticleData> pager =
        new CursorPager<>(
            Arrays.asList(createArticleData("1", time1), createArticleData("2", time2)),
            Direction.NEXT,
            false);

    assertNotNull(pager.getEndCursor());
    assertEquals(time2.getMillis(), ((DateTimeCursor) pager.getEndCursor()).getData().getMillis());
  }

  @Test
  void should_return_null_cursors_for_empty_data() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  void should_return_data() {
    DateTime now = new DateTime();
    ArticleData data = createArticleData("1", now);
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(data), Direction.NEXT, false);

    assertEquals(1, pager.getData().size());
    assertEquals("1", pager.getData().get(0).getId());
  }
}
