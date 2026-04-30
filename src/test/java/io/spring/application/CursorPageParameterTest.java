package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  void should_create_with_valid_params() {
    DateTime cursor = new DateTime();
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 10, Direction.NEXT);

    assertEquals(10, param.getLimit());
    assertEquals(cursor, param.getCursor());
    assertEquals(Direction.NEXT, param.getDirection());
  }

  @Test
  void should_cap_limit_at_max() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 5000, Direction.NEXT);

    assertEquals(1000, param.getLimit());
  }

  @Test
  void should_keep_default_limit_for_zero() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 0, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void should_keep_default_limit_for_negative() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, -5, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void should_return_query_limit_as_limit_plus_one() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.NEXT);

    assertEquals(11, param.getQueryLimit());
  }

  @Test
  void should_return_is_next_true_for_next_direction() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.NEXT);

    assertTrue(param.isNext());
  }

  @Test
  void should_return_is_next_false_for_prev_direction() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.PREV);

    assertFalse(param.isNext());
  }

  @Test
  void should_accept_null_cursor() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.NEXT);

    assertNull(param.getCursor());
  }

  @Test
  void should_use_default_no_arg_constructor() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();

    assertEquals(20, param.getLimit());
    assertNull(param.getCursor());
  }
}
