package io.spring.comments.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
  private String id;
  private String body;
  private String articleId;
  private String userId;
  private String createdAt;
  private String updatedAt;
}
