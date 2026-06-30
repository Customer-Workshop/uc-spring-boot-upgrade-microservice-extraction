package io.spring.infrastructure.service.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentServiceResponse {
  private String id;
  private String body;
  private String articleId;
  private String userId;
  private String createdAt;
  private String updatedAt;
}
