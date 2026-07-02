package io.spring.application;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parameters for offset-based pagination with configurable offset and limit.
 *
 * <p>The limit is capped at {@value #MAX_LIMIT}; negative offsets are ignored.
 */
@NoArgsConstructor
@Data
public class Page {
  private static final int MAX_LIMIT = 100;
  private int offset = 0;
  private int limit = 20;

  public Page(int offset, int limit) {
    setOffset(offset);
    setLimit(limit);
  }

  private void setOffset(int offset) {
    if (offset > 0) {
      this.offset = offset;
    }
  }

  private void setLimit(int limit) {
    if (limit > MAX_LIMIT) {
      this.limit = MAX_LIMIT;
    } else if (limit > 0) {
      this.limit = limit;
    }
  }
}
