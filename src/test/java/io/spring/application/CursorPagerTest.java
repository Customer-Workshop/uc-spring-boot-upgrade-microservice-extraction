package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  private ArticleData createArticleData(String id) {
    DateTime now = new DateTime();
    return new ArticleData(
        id,
        "slug-" + id,
        "title-" + id,
        "desc",
        "body",
        false,
        0,
        now,
        now,
        Collections.emptyList(),
        new ProfileData("user-id", "user", "bio", "image", false));
  }

  @Test
  public void should_have_next_when_direction_next_and_has_extra() {
    ArticleData article = createArticleData("1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_not_have_next_when_direction_next_and_no_extra() {
    ArticleData article = createArticleData("1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_have_previous_when_direction_prev_and_has_extra() {
    ArticleData article = createArticleData("1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  public void should_not_have_previous_when_direction_prev_and_no_extra() {
    ArticleData article = createArticleData("1");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.PREV, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_return_start_cursor_from_first_element() {
    ArticleData article1 = createArticleData("1");
    ArticleData article2 = createArticleData("2");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article1, article2), Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertEquals(article1.getCursor().toString(), pager.getStartCursor().toString());
  }

  @Test
  public void should_return_end_cursor_from_last_element() {
    ArticleData article1 = createArticleData("1");
    ArticleData article2 = createArticleData("2");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article1, article2), Direction.NEXT, false);

    assertNotNull(pager.getEndCursor());
    assertEquals(article2.getCursor().toString(), pager.getEndCursor().toString());
  }

  @Test
  public void should_return_null_cursors_for_empty_data() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  public void should_return_data() {
    ArticleData article = createArticleData("1");
    List<ArticleData> data = Arrays.asList(article);
    CursorPager<ArticleData> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertEquals(1, pager.getData().size());
    assertEquals(article, pager.getData().get(0));
  }
}
