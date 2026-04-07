package io.spring.comments.domain;

import java.time.Instant;
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
public class Comment {

  @Id
  @Column(length = 255)
  private String id;

  @Column(columnDefinition = "CLOB")
  private String body;

  @Column(name = "user_id", length = 255)
  private String userId;

  @Column(name = "article_id", length = 255)
  private String articleId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public Comment(String id, String body, String userId, String articleId) {
    this.id = id;
    this.body = body;
    this.userId = userId;
    this.articleId = articleId;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }
}
