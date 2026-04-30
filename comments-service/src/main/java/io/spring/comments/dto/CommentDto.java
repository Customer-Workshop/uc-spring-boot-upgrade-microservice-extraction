package io.spring.comments.dto;

import io.spring.comments.domain.Comment;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
  private String id;
  private String body;
  private String userId;
  private String articleId;
  private Instant createdAt;
  private Instant updatedAt;

  public static CommentDto fromEntity(Comment comment) {
    return new CommentDto(
        comment.getId(),
        comment.getBody(),
        comment.getUserId(),
        comment.getArticleId(),
        comment.getCreatedAt(),
        comment.getUpdatedAt());
  }
}
