package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  private ArticleData createArticleData(String id) {
    DateTime now = new DateTime();
    return new ArticleData(
        id, "slug-" + id, "title", "desc", "body", false, 0, now, now, null, null);
  }

  @Test
  void should_create_next_direction_with_extra() {
    List<ArticleData> data = Arrays.asList(createArticleData("1"), createArticleData("2"));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
    assertTrue(pager.isNext());
    assertFalse(pager.isPrevious());
    assertEquals(2, pager.getData().size());
  }

  @Test
  void should_create_next_direction_without_extra() {
    List<ArticleData> data = Arrays.asList(createArticleData("1"));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_create_prev_direction_with_extra() {
    List<ArticleData> data = Arrays.asList(createArticleData("1"));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void should_create_prev_direction_without_extra() {
    List<ArticleData> data = Arrays.asList(createArticleData("1"));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_return_start_cursor() {
    List<ArticleData> data = Arrays.asList(createArticleData("1"), createArticleData("2"));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
  }

  @Test
  void should_return_end_cursor() {
    List<ArticleData> data = Arrays.asList(createArticleData("1"), createArticleData("2"));
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertNotNull(pager.getEndCursor());
  }

  @Test
  void should_return_null_cursors_for_empty_data() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }
}
