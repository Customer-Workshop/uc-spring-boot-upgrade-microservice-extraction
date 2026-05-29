package io.spring.articleservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "article_favorites")
@IdClass(ArticleFavoriteId.class)
@Getter
@Setter
@NoArgsConstructor
public class ArticleFavorite {

  @Id
  @Column(name = "article_id", length = 255)
  private String articleId;

  @Id
  @Column(name = "user_id", length = 255)
  private String userId;

  public ArticleFavorite(String articleId, String userId) {
    this.articleId = articleId;
    this.userId = userId;
  }
}
