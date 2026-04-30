package io.spring.comments.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateCommentRequest {
  @NotBlank(message = "body can't be empty")
  private String body;

  @NotBlank(message = "userId can't be empty")
  private String userId;

  @NotBlank(message = "articleId can't be empty")
  private String articleId;
}
