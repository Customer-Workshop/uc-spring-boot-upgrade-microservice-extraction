package io.spring.comments.api;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
  @NotBlank(message = "can't be empty")
  private String body;

  @NotBlank private String userId;

  @NotBlank private String articleId;
}
