package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  void should_create_with_params() {
    DateTime cursor = new DateTime();
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 10, Direction.NEXT);

    assertEquals(cursor, param.getCursor());
    assertEquals(10, param.getLimit());
    assertEquals(Direction.NEXT, param.getDirection());
    assertTrue(param.isNext());
    assertEquals(11, param.getQueryLimit());
  }

  @Test
  void should_create_with_default_values() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();

    assertEquals(20, param.getLimit());
    assertNull(param.getCursor());
  }

  @Test
  void should_cap_limit_at_max() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 2000, Direction.NEXT);

    assertEquals(1000, param.getLimit());
  }

  @Test
  void should_not_change_negative_limit() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, -5, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void should_not_change_zero_limit() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 0, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void should_return_false_for_is_next_when_prev() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.PREV);

    assertFalse(param.isNext());
  }

  @Test
  void should_handle_null_cursor() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.NEXT);

    assertNull(param.getCursor());
  }
}
