package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.junit.jupiter.api.Test;

public class PaginationTest {

  @Test
  public void should_create_cursor_page_parameter_with_next_direction() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 10, Direction.NEXT);
    assertEquals("cursor", param.getCursor());
    assertEquals(10, param.getLimit());
    assertEquals(Direction.NEXT, param.getDirection());
    assertTrue(param.isNext());
    assertEquals(11, param.getQueryLimit());
  }

  @Test
  public void should_create_cursor_page_parameter_with_prev_direction() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 10, Direction.PREV);
    assertFalse(param.isNext());
  }

  @Test
  public void should_limit_max_cursor_page_size() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 2000, Direction.NEXT);
    assertEquals(1000, param.getLimit());
  }

  @Test
  public void should_use_default_limit_for_zero() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 0, Direction.NEXT);
    assertEquals(20, param.getLimit());
  }

  @Test
  public void should_use_default_limit_for_negative() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", -5, Direction.NEXT);
    assertEquals(20, param.getLimit());
  }

  @Test
  public void should_test_cursor_page_parameter_equals_and_hashcode() {
    CursorPageParameter<String> p1 = new CursorPageParameter<>("cursor", 10, Direction.NEXT);
    CursorPageParameter<String> p2 = new CursorPageParameter<>("cursor", 10, Direction.NEXT);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotNull(p1.toString());
  }

  @Test
  public void should_create_page_with_offset_and_limit() {
    Page page = new Page(5, 10);
    assertEquals(5, page.getOffset());
    assertEquals(10, page.getLimit());
  }

  @Test
  public void should_limit_max_page_size() {
    Page page = new Page(0, 200);
    assertEquals(100, page.getLimit());
  }

  @Test
  public void should_use_default_page_for_zero_limit() {
    Page page = new Page(0, 0);
    assertEquals(20, page.getLimit());
  }

  @Test
  public void should_use_default_page_for_negative_offset() {
    Page page = new Page(-5, 10);
    assertEquals(0, page.getOffset());
  }

  @Test
  public void should_test_page_equals_and_hashcode() {
    Page p1 = new Page(5, 10);
    Page p2 = new Page(5, 10);
    assertEquals(p1, p2);
    assertEquals(p1.hashCode(), p2.hashCode());
    assertNotNull(p1.toString());
  }

  @Test
  public void should_test_page_default_constructor() {
    Page page = new Page();
    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  public void should_test_page_inequality() {
    Page p1 = new Page(5, 10);
    Page p2 = new Page(0, 20);
    assertNotEquals(p1, p2);
  }

  @Test
  public void should_test_cursor_pager_next() {
    io.spring.application.data.ArticleData a1 = new io.spring.application.data.ArticleData();
    a1.setUpdatedAt(new org.joda.time.DateTime());
    io.spring.application.data.ArticleData a2 = new io.spring.application.data.ArticleData();
    a2.setUpdatedAt(new org.joda.time.DateTime());

    CursorPager<io.spring.application.data.ArticleData> pager =
        new CursorPager<>(java.util.Arrays.asList(a1, a2), Direction.NEXT, true);

    assertNotNull(pager.getData());
    assertEquals(2, pager.getData().size());
    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  public void should_test_cursor_pager_prev() {
    io.spring.application.data.ArticleData a1 = new io.spring.application.data.ArticleData();
    a1.setUpdatedAt(new org.joda.time.DateTime());

    CursorPager<io.spring.application.data.ArticleData> prevPager =
        new CursorPager<>(java.util.Arrays.asList(a1), Direction.PREV, true);
    assertTrue(prevPager.hasPrevious());
    assertFalse(prevPager.hasNext());
  }

  @Test
  public void should_test_cursor_pager_empty() {
    CursorPager<io.spring.application.data.ArticleData> pager =
        new CursorPager<>(java.util.Collections.emptyList(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
    assertFalse(pager.hasNext());
  }
}
