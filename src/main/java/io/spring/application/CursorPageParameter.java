package io.spring.application;

import io.spring.application.CursorPager.Direction;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parameters for cursor-based pagination including cursor position, page limit, and traversal
 * direction.
 *
 * @param <T> the cursor data type (e.g., {@link org.joda.time.DateTime})
 */
@Data
@NoArgsConstructor
public class CursorPageParameter<T> {
  private static final int MAX_LIMIT = 1000;
  private int limit = 20;
  private T cursor;
  private Direction direction;

  public CursorPageParameter(T cursor, int limit, Direction direction) {
    setLimit(limit);
    setCursor(cursor);
    setDirection(direction);
  }

  public boolean isNext() {
    return direction == Direction.NEXT;
  }

  public int getQueryLimit() {
    return limit + 1;
  }

  private void setCursor(T cursor) {
    this.cursor = cursor;
  }

  private void setLimit(int limit) {
    if (limit > MAX_LIMIT) {
      this.limit = MAX_LIMIT;
    } else if (limit > 0) {
      this.limit = limit;
    }
  }
}
