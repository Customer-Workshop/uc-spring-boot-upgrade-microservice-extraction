package io.spring.comments.api;

import io.spring.comments.domain.Comment;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

  private String id;
  private String body;
  private String userId;
  private String articleId;
  private Instant createdAt;
  private Instant updatedAt;

  public static CommentResponse fromEntity(Comment comment) {
    return new CommentResponse(
        comment.getId(),
        comment.getBody(),
        comment.getUserId(),
        comment.getArticleId(),
        comment.getCreatedAt(),
        comment.getUpdatedAt());
  }
}
