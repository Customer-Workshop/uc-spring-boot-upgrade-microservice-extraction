package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  public void should_use_default_limit_for_negative() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, -1, Direction.NEXT);
    assertEquals(20, page.getLimit());
  }

  @Test
  public void should_use_default_limit_for_zero() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 0, Direction.NEXT);
    assertEquals(20, page.getLimit());
  }

  @Test
  public void should_cap_at_max_limit() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 2000, Direction.NEXT);
    assertEquals(1000, page.getLimit());
  }

  @Test
  public void should_use_provided_limit() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 50, Direction.NEXT);
    assertEquals(50, page.getLimit());
  }

  @Test
  public void should_return_query_limit_plus_one() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    assertEquals(11, page.getQueryLimit());
  }

  @Test
  public void should_be_next_for_next_direction() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    assertTrue(page.isNext());
  }

  @Test
  public void should_not_be_next_for_prev_direction() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    assertFalse(page.isNext());
  }

  @Test
  public void should_store_cursor() {
    DateTime cursor = new DateTime(2020, 1, 1, 0, 0);
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(cursor, 10, Direction.NEXT);
    assertEquals(cursor, page.getCursor());
  }
}
