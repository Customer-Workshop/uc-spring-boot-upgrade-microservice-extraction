package io.spring.article.application;

import java.util.List;
import lombok.Getter;

@Getter
public class CursorPager<T extends Node> {
  private List<T> data;
  private boolean hasExtra;
  private CursorPageParameter.Direction direction;

  public CursorPager(List<T> data, CursorPageParameter.Direction direction, boolean hasExtra) {
    this.data = data;
    this.direction = direction;
    this.hasExtra = hasExtra;
  }

  public PageCursor<T> getStartCursor() {
    return data.isEmpty() ? null : new PageCursor<>(data.get(0));
  }

  public PageCursor<T> getEndCursor() {
    return data.isEmpty() ? null : new PageCursor<>(data.get(data.size() - 1));
  }

  public boolean hasNextPage() {
    if (direction == CursorPageParameter.Direction.NEXT) {
      return hasExtra;
    }
    return true;
  }

  public boolean hasPreviousPage() {
    if (direction == CursorPageParameter.Direction.PREV) {
      return hasExtra;
    }
    return true;
  }
}
