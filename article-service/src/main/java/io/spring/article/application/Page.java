package io.spring.article.application;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Page {
  private static final int MAX_LIMIT = 100;
  private int offset = 0;
  private int limit = 20;

  public Page(int offset, int limit) {
    this.offset = Math.max(offset, 0);
    this.limit = Math.min(limit, MAX_LIMIT);
  }
}
