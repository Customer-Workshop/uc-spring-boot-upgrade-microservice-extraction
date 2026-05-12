package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  void should_create_with_next_direction() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.NEXT);

    assertTrue(param.isNext());
    assertEquals(10, param.getLimit());
    assertEquals(11, param.getQueryLimit());
    assertNull(param.getCursor());
  }

  @Test
  void should_create_with_prev_direction() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.PREV);

    assertFalse(param.isNext());
  }

  @Test
  void should_cap_limit_at_max() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 2000, Direction.NEXT);

    assertEquals(1000, param.getLimit());
    assertEquals(1001, param.getQueryLimit());
  }

  @Test
  void should_use_default_limit_for_zero_or_negative() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 0, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void should_use_default_limit_for_negative() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, -5, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void should_store_cursor() {
    DateTime cursor = new DateTime();
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 10, Direction.NEXT);

    assertEquals(cursor, param.getCursor());
  }
}
