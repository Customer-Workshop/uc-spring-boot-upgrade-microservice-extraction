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
  }

  @Test
  void should_create_with_valid_params() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor1", 50, Direction.NEXT);

    assertEquals(50, param.getLimit());
    assertEquals("cursor1", param.getCursor());
    assertEquals(Direction.NEXT, param.getDirection());
  }

  @Test
  void should_cap_limit_to_max() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 2000, Direction.NEXT);

    assertEquals(1000, param.getLimit());
  }

  @Test
  void should_use_default_limit_for_zero() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 0, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void should_use_default_limit_for_negative() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", -5, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void should_return_true_for_next_direction() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 10, Direction.NEXT);

    assertTrue(param.isNext());
  }

  @Test
  void should_return_false_for_prev_direction() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 10, Direction.PREV);

    assertFalse(param.isNext());
  }

  @Test
  void should_return_query_limit_as_limit_plus_one() {
    CursorPageParameter<String> param = new CursorPageParameter<>("cursor", 10, Direction.NEXT);

    assertEquals(11, param.getQueryLimit());
  }
}
