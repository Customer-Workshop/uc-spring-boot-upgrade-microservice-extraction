package io.spring.interaction.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class ArticleFavorite {
  private String articleId;
  private String userId;

  public ArticleFavorite(String articleId, String userId) {
    this.articleId = articleId;
    this.userId = userId;
  }
}
