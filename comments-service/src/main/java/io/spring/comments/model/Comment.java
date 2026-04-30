package io.spring.comments.model;

import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
public class Comment {

  @Id private String id;

  @Lob
  @Column(nullable = false)
  private String body;

  @Column(name = "article_id", nullable = false)
  private String articleId;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public Comment(String body, String articleId, String userId) {
    this.id = UUID.randomUUID().toString();
    this.body = body;
    this.articleId = articleId;
    this.userId = userId;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }
}
