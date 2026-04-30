package io.spring.comments.api;

import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateCommentRequest {
  @NotBlank(message = "can't be empty")
  private String body;

  @NotBlank private String userId;

  @NotBlank private String articleId;
}
