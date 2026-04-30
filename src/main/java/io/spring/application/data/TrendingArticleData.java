package io.spring.application.data;

import lombok.Value;

@Value
public class TrendingArticleData {
  private String slug;
  private String title;
  private String description;
  private String author;
  private int favoriteCount;
}
