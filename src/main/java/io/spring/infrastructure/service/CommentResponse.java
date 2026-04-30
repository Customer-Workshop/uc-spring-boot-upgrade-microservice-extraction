package io.spring.infrastructure.service;

import io.spring.core.comment.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
public class CommentResponse {
  private String id;
  private String body;
  private String userId;
  private String articleId;
  private String createdAt;
  private String updatedAt;

  public Comment toComment() {
    DateTime created = createdAt != null ? DateTime.parse(createdAt) : new DateTime();
    return new Comment(id, body, userId, articleId, created);
  }
}
