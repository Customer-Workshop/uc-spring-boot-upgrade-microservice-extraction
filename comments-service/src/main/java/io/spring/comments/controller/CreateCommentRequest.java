package io.spring.comments.controller;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentRequest {
  @NotBlank(message = "body can't be empty")
  private String body;

  @NotBlank(message = "userId can't be empty")
  private String userId;

  @NotBlank(message = "articleId can't be empty")
  private String articleId;
}
