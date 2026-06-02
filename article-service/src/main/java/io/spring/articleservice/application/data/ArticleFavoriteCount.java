package io.spring.articleservice.application.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticleFavoriteCount {
  private String id;
  private int count;
}
