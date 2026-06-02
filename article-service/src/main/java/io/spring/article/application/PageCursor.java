package io.spring.article.application;

import lombok.Getter;

@Getter
public class PageCursor<T extends Node> {
  private T node;

  public PageCursor(T node) {
    this.node = node;
  }

  @Override
  public String toString() {
    return node.getCursor().toString();
  }
}
