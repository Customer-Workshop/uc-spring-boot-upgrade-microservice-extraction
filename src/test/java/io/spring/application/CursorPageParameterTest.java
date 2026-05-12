package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  void should_create_with_defaults() {
    CursorPageParameter<String> param = new CursorPageParameter<>();
    assertEquals(20, param.getLimit());
    assertNull(param.getCursor());
    assertNull(param.getDirection());
  }

  @Test
  void should_create_with_custom_values() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 10, Direction.NEXT);
    assertEquals(10, param.getLimit());
    assertEquals("cursor", param.getCursor());
    assertEquals(Direction.NEXT, param.getDirection());
  }

  @Test
  void should_return_query_limit_as_limit_plus_one() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 10, Direction.NEXT);
    assertEquals(11, param.getQueryLimit());
  }

  @Test
  void should_return_is_next_true_for_next_direction() {
    CursorPageParameter<String> param = new CursorPageParameter<>(null, 10, Direction.NEXT);
    assertTrue(param.isNext());
  }

  @Test
  void should_return_is_next_false_for_prev_direction() {
    CursorPageParameter<String> param = new CursorPageParameter<>(null, 10, Direction.PREV);
    assertFalse(param.isNext());
  }

  @Test
  void should_cap_limit_at_max() {
    CursorPageParameter<String> param = new CursorPageParameter<>(null, 2000, Direction.NEXT);
    assertEquals(1000, param.getLimit());
  }

  @Test
  void should_not_allow_negative_limit() {
    CursorPageParameter<String> param = new CursorPageParameter<>(null, -5, Direction.NEXT);
    assertEquals(20, param.getLimit());
  }

  @Test
  void should_not_allow_zero_limit() {
    CursorPageParameter<String> param = new CursorPageParameter<>(null, 0, Direction.NEXT);
    assertEquals(20, param.getLimit());
  }
}
