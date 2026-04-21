package io.spring.application.data;

import lombok.Value;

// TODO: This DTO has been extracted to the Favorite Service (favorite-service/).
// Once the Favorite Service is fully deployed, remove this class.
@Value
public class ArticleFavoriteCount {
  private String id;
  private Integer count;
}
