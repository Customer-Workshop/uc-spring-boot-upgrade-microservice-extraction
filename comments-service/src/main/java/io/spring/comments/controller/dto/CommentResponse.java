package io.spring.comments.controller.dto;

import io.spring.comments.model.Comment;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

  private String id;
  private String body;
  private String articleId;
  private String userId;
  private Instant createdAt;
  private Instant updatedAt;

  public static CommentResponse fromEntity(Comment comment) {
    return new CommentResponse(
        comment.getId(),
        comment.getBody(),
        comment.getArticleId(),
        comment.getUserId(),
        comment.getCreatedAt(),
        comment.getUpdatedAt());
  }
}
