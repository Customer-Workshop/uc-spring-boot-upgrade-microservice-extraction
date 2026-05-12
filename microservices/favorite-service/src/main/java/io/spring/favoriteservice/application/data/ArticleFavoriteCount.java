package io.spring.favoriteservice.application.data;

import lombok.Value;

@Value
public class ArticleFavoriteCount {
  private String id;
  private Integer count;
}
