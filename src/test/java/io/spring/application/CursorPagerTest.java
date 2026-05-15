package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.data.ArticleData;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  @Test
  public void should_have_next_when_direction_is_next_and_has_extra() {
    List<ArticleData> data = createArticleDataList(2);
    CursorPager<ArticleData> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, true);
    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_not_have_next_when_direction_is_next_and_no_extra() {
    List<ArticleData> data = createArticleDataList(2);
    CursorPager<ArticleData> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_have_previous_when_direction_is_prev_and_has_extra() {
    List<ArticleData> data = createArticleDataList(2);
    CursorPager<ArticleData> pager = new CursorPager<>(data, CursorPager.Direction.PREV, true);
    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  public void should_return_start_cursor_from_first_element() {
    List<ArticleData> data = createArticleDataList(2);
    CursorPager<ArticleData> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);
    assertNotNull(pager.getStartCursor());
    assertEquals(data.get(0).getCursor().getData(), pager.getStartCursor().getData());
  }

  @Test
  public void should_return_end_cursor_from_last_element() {
    List<ArticleData> data = createArticleDataList(2);
    CursorPager<ArticleData> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);
    assertNotNull(pager.getEndCursor());
    assertEquals(data.get(1).getCursor().getData(), pager.getEndCursor().getData());
  }

  @Test
  public void should_return_null_cursors_for_empty_data() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(new ArrayList<>(), CursorPager.Direction.NEXT, false);
    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  public void should_return_data() {
    List<ArticleData> data = createArticleDataList(3);
    CursorPager<ArticleData> pager = new CursorPager<>(data, CursorPager.Direction.NEXT, false);
    assertEquals(3, pager.getData().size());
  }

  private List<ArticleData> createArticleDataList(int count) {
    List<ArticleData> list = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      ArticleData articleData = new ArticleData();
      articleData.setUpdatedAt(new DateTime().plusMinutes(i));
      list.add(articleData);
    }
    return list;
  }
}
