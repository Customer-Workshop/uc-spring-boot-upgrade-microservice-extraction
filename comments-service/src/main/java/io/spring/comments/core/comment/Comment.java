package io.spring.comments.core.comment;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Comment {
  private String id;
  private String body;
  private String userId;
  private String articleId;
  private DateTime createdAt;

  public Comment(String body, String userId, String articleId) {
    this(UUID.randomUUID().toString(), body, userId, articleId, new DateTime());
  }

  public Comment(String id, String body, String userId, String articleId, DateTime createdAt) {
    this.id = id == null ? UUID.randomUUID().toString() : id;
    this.body = body;
    this.userId = userId;
    this.articleId = articleId;
    this.createdAt = createdAt == null ? new DateTime() : createdAt;
  }
}
