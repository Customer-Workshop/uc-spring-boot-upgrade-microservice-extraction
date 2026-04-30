package io.spring.application.data;

import lombok.Value;

@Value
public class ArticleStatsData {
  private String slug;
  private int viewCount;
  private int favoriteCount;
  private int commentCount;
  private long daysSincePublished;
}
