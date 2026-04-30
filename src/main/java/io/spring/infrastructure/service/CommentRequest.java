package io.spring.infrastructure.service;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentRequest {
  private String body;
  private String userId;
  private String articleId;
}
