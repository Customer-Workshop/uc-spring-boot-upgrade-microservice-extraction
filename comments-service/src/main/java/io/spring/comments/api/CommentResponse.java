package io.spring.comments.api;

import io.spring.comments.core.comment.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

/** Raw comment projection (no author enrichment) used by the monolith for persistence and authz. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
  private String id;
  private String body;
  private String articleId;
  private String userId;
  private DateTime createdAt;

  public static CommentResponse from(Comment comment) {
    return new CommentResponse(
        comment.getId(),
        comment.getBody(),
        comment.getArticleId(),
        comment.getUserId(),
        comment.getCreatedAt());
  }
}
