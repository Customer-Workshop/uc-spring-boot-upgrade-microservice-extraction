package io.spring.favorites.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleFavoriteData {
  private String articleId;
  private int favoritesCount;
  private boolean favorited;
}
