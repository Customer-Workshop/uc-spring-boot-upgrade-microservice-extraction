package io.spring.comments.model;

import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
public class CommentEntity {

  @Id private String id;

  @Column(nullable = false)
  private String body;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "article_id", nullable = false)
  private String articleId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public CommentEntity(String body, String userId, String articleId) {
    this.id = UUID.randomUUID().toString();
    this.body = body;
    this.userId = userId;
    this.articleId = articleId;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public CommentEntity(String id, String body, String userId, String articleId) {
    this.id = id;
    this.body = body;
    this.userId = userId;
    this.articleId = articleId;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }
}
