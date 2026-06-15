package io.spring.application;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
public class Page {
  private static final int MAX_LIMIT = 100;
  private int offset;
  private int limit;

  public Page(int offset, int limit) {
    this.offset = Math.max(offset, 0);
    this.limit = Math.min(limit, MAX_LIMIT);
  }
}
