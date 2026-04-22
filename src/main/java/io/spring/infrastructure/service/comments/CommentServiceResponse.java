package io.spring.infrastructure.service.comments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentServiceResponse {
  private String id;
  private String body;
  private String userId;
  private String articleId;
  private String createdAt;
}
