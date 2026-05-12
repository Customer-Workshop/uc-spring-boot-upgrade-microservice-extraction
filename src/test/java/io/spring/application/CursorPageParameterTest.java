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

    assertEquals(cursor, param.getCursor());
    assertEquals(10, param.getLimit());
    assertEquals(Direction.NEXT, param.getDirection());
    assertTrue(param.isNext());
  }

  @Test
  void should_return_query_limit_as_limit_plus_one() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 20, Direction.NEXT);

    assertEquals(21, param.getQueryLimit());
  }

  @Test
  void should_cap_limit_at_max() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 5000, Direction.NEXT);

    assertEquals(1000, param.getLimit());
    assertEquals(1001, param.getQueryLimit());
  }

  @Test
  void should_not_change_limit_when_zero_or_negative() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 0, Direction.NEXT);
    assertEquals(20, param.getLimit());

    CursorPageParameter<DateTime> param2 = new CursorPageParameter<>(null, -5, Direction.NEXT);
    assertEquals(20, param2.getLimit());
  }

  @Test
  void should_report_is_next_false_for_prev() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.PREV);
    assertFalse(param.isNext());
  }

  @Test
  void should_handle_null_cursor() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.NEXT);
    assertNull(param.getCursor());
  }

  @Test
  void should_create_with_no_args() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();
    assertEquals(20, param.getLimit());
    assertNull(param.getCursor());
  }
}
