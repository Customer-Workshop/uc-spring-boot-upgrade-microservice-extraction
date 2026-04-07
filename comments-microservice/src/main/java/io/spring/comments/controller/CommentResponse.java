package io.spring.comments.controller;

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
  private String userId;
  private String articleId;
  private String createdAt;
  private String updatedAt;
}
