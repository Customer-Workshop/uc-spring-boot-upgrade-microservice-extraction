package io.spring.articleservice.application;

import lombok.Data;

@Data
public class Page {
  private int offset;
  private int limit;

  public Page(int offset, int limit) {
    this.offset = offset;
    this.limit = limit;
  }
}
