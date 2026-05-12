package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  private ArticleData createArticleData(String slug, DateTime updatedAt) {
    ProfileData profile = new ProfileData("uid", "user", "bio", "img", false);
    return new ArticleData(
        "id-" + slug,
        slug,
        "Title",
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        updatedAt,
        Arrays.asList(),
        profile);
  }

  @Test
  void should_create_next_direction_pager_with_extra() {
    ArticleData a1 = createArticleData("a1", new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
    assertEquals(1, pager.getData().size());
  }

  @Test
  void should_create_next_direction_pager_without_extra() {
    ArticleData a1 = createArticleData("a1", new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_create_prev_direction_pager_with_extra() {
    ArticleData a1 = createArticleData("a1", new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void should_create_prev_direction_pager_without_extra() {
    ArticleData a1 = createArticleData("a1", new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.PREV, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_return_null_cursors_for_empty_data() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  void should_return_start_and_end_cursors() {
    DateTime t1 = new DateTime(2023, 1, 1, 0, 0);
    DateTime t2 = new DateTime(2023, 1, 2, 0, 0);
    ArticleData a1 = createArticleData("a1", t1);
    ArticleData a2 = createArticleData("a2", t2);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(a1, a2), Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
    assertEquals(String.valueOf(t1.getMillis()), pager.getStartCursor().toString());
    assertEquals(String.valueOf(t2.getMillis()), pager.getEndCursor().toString());
  }
}
