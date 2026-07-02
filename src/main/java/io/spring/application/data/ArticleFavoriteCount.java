package io.spring.application.data;

import lombok.Value;

/** Immutable DTO pairing an article identifier with its favorite count. */
@Value
public class ArticleFavoriteCount {
  private String id;
  private Integer count;
}
