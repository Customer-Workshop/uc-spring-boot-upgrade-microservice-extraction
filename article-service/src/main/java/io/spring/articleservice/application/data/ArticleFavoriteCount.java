package io.spring.articleservice.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArticleFavoriteCount {
  private String id;
  private int count;
}
