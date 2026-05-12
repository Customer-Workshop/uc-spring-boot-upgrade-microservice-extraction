package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  @Test
  void should_indicate_has_next_when_direction_is_next_and_has_extra() {
    List<ArticleData> data = createArticleDataList(3);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_indicate_has_previous_when_direction_is_prev_and_has_extra() {
    List<ArticleData> data = createArticleDataList(3);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void should_not_indicate_has_next_when_direction_is_next_and_no_extra() {
    List<ArticleData> data = createArticleDataList(3);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_not_indicate_has_previous_when_direction_is_prev_and_no_extra() {
    List<ArticleData> data = createArticleDataList(3);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.PREV, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_return_null_cursors_for_empty_data() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  void should_return_correct_start_and_end_cursors() {
    List<ArticleData> data = createArticleDataList(3);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
    assertEquals(data.get(0).getCursor().toString(), pager.getStartCursor().toString());
    assertEquals(data.get(2).getCursor().toString(), pager.getEndCursor().toString());
  }

  @Test
  void should_return_data_list() {
    List<ArticleData> data = createArticleDataList(5);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertEquals(5, pager.getData().size());
  }

  private List<ArticleData> createArticleDataList(int count) {
    List<ArticleData> list = new ArrayList<>();
    DateTime base = new DateTime();
    for (int i = 0; i < count; i++) {
      DateTime time = base.minusMinutes(i);
      ArticleData article =
          new ArticleData(
              "id" + i,
              "slug-" + i,
              "title " + i,
              "desc " + i,
              "body " + i,
              false,
              0,
              time,
              time,
              Arrays.asList(),
              new ProfileData("userId", "user", "bio", "image", false));
      list.add(article);
    }
    return list;
  }
}
