package io.spring.comments.controller;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateCommentRequest {

  @NotBlank(message = "can't be empty")
  private String body;

  @NotBlank(message = "can't be empty")
  private String userId;

  @NotBlank(message = "can't be empty")
  private String articleId;
}
