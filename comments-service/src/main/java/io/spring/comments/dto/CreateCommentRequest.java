package io.spring.comments.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
  private String id;

  @NotBlank(message = "body can't be empty")
  private String body;

  @NotBlank(message = "userId can't be empty")
  private String userId;

  @NotBlank(message = "articleId can't be empty")
  private String articleId;
}
