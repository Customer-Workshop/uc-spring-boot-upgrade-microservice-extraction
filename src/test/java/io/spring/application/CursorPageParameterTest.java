package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  public void should_create_default_cursor_page_parameter() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();
    assertEquals(20, param.getLimit());
    assertNull(param.getCursor());
  }

  @Test
  public void should_create_with_valid_params() {
    DateTime cursor = new DateTime();
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 10, Direction.NEXT);
    assertEquals(10, param.getLimit());
    assertEquals(cursor, param.getCursor());
    assertEquals(Direction.NEXT, param.getDirection());
    assertTrue(param.isNext());
  }

  @Test
  public void should_return_query_limit_plus_one() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.NEXT);
    assertEquals(11, param.getQueryLimit());
  }

  @Test
  public void should_cap_limit_at_max() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 2000, Direction.NEXT);
    assertEquals(1000, param.getLimit());
  }

  @Test
  public void should_ignore_negative_limit() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, -5, Direction.NEXT);
    assertEquals(20, param.getLimit());
  }

  @Test
  public void should_not_be_next_when_prev() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.PREV);
    assertFalse(param.isNext());
  }
}
