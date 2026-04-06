package io.spring.comments.controller.dto;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCommentRequest {

  @NotBlank(message = "can't be empty")
  private String body;

  private String userId;

  @NotBlank(message = "can't be empty")
  private String articleId;
}
