package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  @Test
  void should_have_next_when_direction_next_and_has_extra() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(List.of(createArticleData("1")), Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_have_previous_when_direction_prev_and_has_extra() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(List.of(createArticleData("1")), Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void should_not_have_next_or_previous_when_no_extra() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(List.of(createArticleData("1")), Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_return_start_cursor() {
    ArticleData article = createArticleData("1");
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(article), Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertEquals(article.getCursor().toString(), pager.getStartCursor().toString());
  }

  @Test
  void should_return_end_cursor() {
    ArticleData a1 = createArticleData("1");
    ArticleData a2 = createArticleData("2");
    CursorPager<ArticleData> pager = new CursorPager<>(List.of(a1, a2), Direction.NEXT, false);

    assertNotNull(pager.getEndCursor());
    assertEquals(a2.getCursor().toString(), pager.getEndCursor().toString());
  }

  @Test
  void should_return_null_cursors_when_empty() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  private ArticleData createArticleData(String id) {
    return new ArticleData(
        id,
        "slug-" + id,
        "title",
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        List.of(),
        new ProfileData("userId", "user", "bio", "image", false));
  }
}
