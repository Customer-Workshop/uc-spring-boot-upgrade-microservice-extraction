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

  private ArticleData createArticleData(String id, DateTime updatedAt) {
    ProfileData profile = new ProfileData("uid", "user", "", "", false);
    ArticleData data = new ArticleData();
    data.setId(id);
    data.setSlug("slug-" + id);
    data.setTitle("title");
    data.setDescription("desc");
    data.setBody("body");
    data.setFavorited(false);
    data.setFavoritesCount(0);
    data.setCreatedAt(new DateTime());
    data.setUpdatedAt(updatedAt);
    data.setTagList(Arrays.asList("java"));
    data.setProfileData(profile);
    return data;
  }

  @Test
  void should_create_pager_with_next_direction_and_extra() {
    ArticleData a1 = createArticleData("1", new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
    assertEquals(1, pager.getData().size());
  }

  @Test
  void should_create_pager_with_next_direction_no_extra() {
    ArticleData a1 = createArticleData("1", new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_create_pager_with_prev_direction_and_extra() {
    ArticleData a1 = createArticleData("1", new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void should_create_pager_with_prev_direction_no_extra() {
    ArticleData a1 = createArticleData("1", new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.PREV, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_return_null_cursors_when_empty() {
    CursorPager<ArticleData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  void should_return_start_and_end_cursors() {
    DateTime dt1 = new DateTime().minusHours(1);
    DateTime dt2 = new DateTime();
    ArticleData a1 = createArticleData("1", dt1);
    ArticleData a2 = createArticleData("2", dt2);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(a1, a2), Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
    assertEquals(dt1, pager.getStartCursor().getData());
    assertEquals(dt2, pager.getEndCursor().getData());
  }
}
