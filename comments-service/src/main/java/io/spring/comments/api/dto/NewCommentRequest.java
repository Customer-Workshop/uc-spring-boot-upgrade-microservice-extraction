package io.spring.comments.api.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewCommentRequest {
  /** Optional caller-provided identity; the monolith supplies its own comment id. */
  private String id;

  @NotBlank(message = "can't be empty")
  private String body;

  @NotBlank(message = "can't be empty")
  private String articleId;

  @NotBlank(message = "can't be empty")
  private String userId;
}
