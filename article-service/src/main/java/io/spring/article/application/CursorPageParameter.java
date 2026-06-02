package io.spring.article.application;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CursorPageParameter<T> {
  private static final int MAX_LIMIT = 1000;
  private int limit = 20;
  private T cursor;
  private Direction direction;

  public CursorPageParameter(T cursor, int limit, Direction direction) {
    this.cursor = cursor;
    this.limit = Math.min(limit, MAX_LIMIT);
    this.direction = direction;
  }

  public boolean isNext() {
    return direction == Direction.NEXT;
  }

  public int getQueryLimit() {
    return limit + 1;
  }

  public enum Direction {
    NEXT,
    PREV
  }
}
