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

  private ArticleData createSampleArticle() {
    return new ArticleData(
        "id",
        "slug",
        "title",
        "desc",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        Collections.emptyList(),
        new ProfileData("uid", "user", "bio", "img", false));
  }

  @Test
  public void should_create_next_pager_with_extra() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(createSampleArticle()), Direction.NEXT, true);
    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_create_next_pager_without_extra() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(createSampleArticle()), Direction.NEXT, false);
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_create_prev_pager_with_extra() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(createSampleArticle()), Direction.PREV, true);
    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  public void should_create_prev_pager_without_extra() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(createSampleArticle()), Direction.PREV, false);
    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_get_start_cursor() {
    ArticleData article = createSampleArticle();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(article), Direction.NEXT, false);
    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
  }

  @Test
  public void should_return_null_cursors_when_empty() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);
    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }
}
