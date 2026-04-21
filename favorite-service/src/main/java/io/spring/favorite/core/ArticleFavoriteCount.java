package io.spring.favorite.core;

import lombok.Value;

@Value
public class ArticleFavoriteCount {
  private String id;
  private Integer count;
}
