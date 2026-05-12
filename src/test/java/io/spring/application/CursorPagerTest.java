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

  private ArticleData createArticleData(String id, String slug) {
    return new ArticleData(
        id,
        slug,
        "Title",
        "Desc",
        "Body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        Collections.emptyList(),
        new ProfileData("user-id", "testuser", "bio", "image", false));
  }

  @Test
  public void should_have_next_when_direction_is_next_with_extra() {
    ArticleData data = createArticleData("id1", "slug1");
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(data), Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_not_have_next_when_direction_is_next_without_extra() {
    ArticleData data = createArticleData("id1", "slug1");
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(data), Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_have_previous_when_direction_is_prev_with_extra() {
    ArticleData data = createArticleData("id1", "slug1");
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(data), Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  public void should_not_have_previous_when_direction_is_prev_without_extra() {
    ArticleData data = createArticleData("id1", "slug1");
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(data), Direction.PREV, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_return_null_cursors_for_empty_data() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  public void should_return_start_and_end_cursors() {
    ArticleData data1 = createArticleData("id1", "slug1");
    ArticleData data2 = createArticleData("id2", "slug2");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(data1, data2), Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
  }

  @Test
  public void should_get_data_list() {
    ArticleData data1 = createArticleData("id1", "slug1");
    ArticleData data2 = createArticleData("id2", "slug2");
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(data1, data2), Direction.NEXT, false);

    assertEquals(2, pager.getData().size());
  }
}
