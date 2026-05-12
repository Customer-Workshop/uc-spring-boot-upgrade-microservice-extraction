package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.junit.jupiter.api.Test;

public class CursorPageParameterTest {

  @Test
  void should_create_with_valid_params() {
    CursorPageParameter<String> page = new CursorPageParameter<>("cursor", 10, Direction.NEXT);
    assertEquals(10, page.getLimit());
    assertEquals("cursor", page.getCursor());
    assertTrue(page.isNext());
    assertEquals(11, page.getQueryLimit());
  }

  @Test
  void should_cap_limit_at_max() {
    CursorPageParameter<String> page = new CursorPageParameter<>("cursor", 2000, Direction.NEXT);
    assertEquals(1000, page.getLimit());
  }

  @Test
  void should_keep_default_limit_when_zero_or_negative() {
    CursorPageParameter<String> page = new CursorPageParameter<>("cursor", 0, Direction.NEXT);
    assertEquals(20, page.getLimit());

    CursorPageParameter<String> page2 = new CursorPageParameter<>("cursor", -5, Direction.NEXT);
    assertEquals(20, page2.getLimit());
  }

  @Test
  void should_return_false_for_is_next_when_prev() {
    CursorPageParameter<String> page = new CursorPageParameter<>("cursor", 10, Direction.PREV);
    assertFalse(page.isNext());
  }
}
