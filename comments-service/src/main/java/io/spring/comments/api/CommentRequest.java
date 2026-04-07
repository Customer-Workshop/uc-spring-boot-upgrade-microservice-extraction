package io.spring.comments.api;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

  @NotBlank(message = "id is required")
  private String id;

  @NotBlank(message = "body is required")
  private String body;

  @NotBlank(message = "userId is required")
  private String userId;

  @NotBlank(message = "articleId is required")
  private String articleId;
}
