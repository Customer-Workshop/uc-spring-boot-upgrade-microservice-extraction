package io.spring.core.favorite;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value object representing a user's favorite relationship with an article.
 *
 * <p>Composite key of {@code articleId} and {@code userId}.
 */
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class ArticleFavorite {
  private String articleId;
  private String userId;

  public ArticleFavorite(String articleId, String userId) {
    this.articleId = articleId;
    this.userId = userId;
  }
}
