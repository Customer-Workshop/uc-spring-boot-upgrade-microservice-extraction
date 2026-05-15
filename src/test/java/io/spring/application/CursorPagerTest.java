package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CursorPagerTest {

  private static class TestNode implements Node {
    private final DateTimeCursor cursor;

    TestNode(DateTime dateTime) {
      this.cursor = new DateTimeCursor(dateTime);
    }

    @Override
    public PageCursor getCursor() {
      return cursor;
    }
  }

  @Test
  void should_have_next_when_direction_is_next_and_has_extra() {
    List<TestNode> data = Arrays.asList(new TestNode(new DateTime()), new TestNode(new DateTime()));
    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_not_have_next_when_direction_is_next_and_no_extra() {
    List<TestNode> data = Arrays.asList(new TestNode(new DateTime()), new TestNode(new DateTime()));
    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_have_previous_when_direction_is_prev_and_has_extra() {
    List<TestNode> data = Arrays.asList(new TestNode(new DateTime()), new TestNode(new DateTime()));
    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void should_not_have_previous_when_direction_is_prev_and_no_extra() {
    List<TestNode> data = Arrays.asList(new TestNode(new DateTime()), new TestNode(new DateTime()));
    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.PREV, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void should_return_null_cursors_when_data_is_empty() {
    CursorPager<TestNode> pager = new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
    assertNull(pager.getEndCursor());
  }

  @Test
  void should_return_start_and_end_cursors() {
    DateTime dt1 = new DateTime(1000);
    DateTime dt2 = new DateTime(2000);
    List<TestNode> data = Arrays.asList(new TestNode(dt1), new TestNode(dt2));
    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertNotNull(pager.getEndCursor());
    assertEquals(dt1, ((DateTimeCursor) pager.getStartCursor()).getData());
    assertEquals(dt2, ((DateTimeCursor) pager.getEndCursor()).getData());
  }

  @Test
  void should_return_data_list() {
    TestNode node1 = new TestNode(new DateTime());
    TestNode node2 = new TestNode(new DateTime());
    List<TestNode> data = Arrays.asList(node1, node2);
    CursorPager<TestNode> pager = new CursorPager<>(data, Direction.NEXT, false);

    assertEquals(2, pager.getData().size());
  }
}
