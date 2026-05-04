package io.spring.comments.api.dto;

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
  private String createdAt;
}
