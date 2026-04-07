package io.spring.comments.controller;

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
}
