package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  public void should_use_default_values() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();
    assertEquals(20, param.getLimit());
    assertNull(param.getCursor());
  }

  @Test
  public void should_set_cursor_and_limit() {
    DateTime cursor = new DateTime();
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(cursor, 10, CursorPager.Direction.NEXT);
    assertEquals(10, param.getLimit());
    assertEquals(cursor, param.getCursor());
    assertEquals(CursorPager.Direction.NEXT, param.getDirection());
  }

  @Test
  public void should_cap_limit_at_max() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(null, 2000, CursorPager.Direction.NEXT);
    assertEquals(1000, param.getLimit());
  }

  @Test
  public void should_not_set_negative_limit() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(null, -5, CursorPager.Direction.NEXT);
    assertEquals(20, param.getLimit());
  }

  @Test
  public void should_return_query_limit_plus_one() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    assertEquals(11, param.getQueryLimit());
  }

  @Test
  public void should_return_true_for_next_direction() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    assertTrue(param.isNext());
  }

  @Test
  public void should_return_false_for_prev_direction() {
    CursorPageParameter<DateTime> param =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.PREV);
    assertFalse(param.isNext());
  }
}
