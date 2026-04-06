package io.spring.article.application;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Page {
  private int offset;
  private int limit;

  public Page(int offset, int limit) {
    this.offset = offset;
    this.limit = limit;
  }
}
