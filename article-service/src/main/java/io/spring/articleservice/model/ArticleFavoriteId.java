package io.spring.articleservice.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ArticleFavoriteId implements Serializable {
  private String articleId;
  private String userId;
}
