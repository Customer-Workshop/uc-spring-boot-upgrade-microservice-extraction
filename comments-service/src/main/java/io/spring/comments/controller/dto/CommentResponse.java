package io.spring.comments.controller.dto;

import io.spring.comments.model.CommentEntity;
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

  public static CommentResponse fromEntity(CommentEntity entity) {
    return new CommentResponse(
        entity.getId(),
        entity.getBody(),
        entity.getArticleId(),
        entity.getUserId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
