package io.spring.comments.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
  @NotBlank(message = "body can't be empty")
  private String body;

  @NotBlank(message = "articleId can't be empty")
  private String articleId;

  @NotBlank(message = "userId can't be empty")
  private String userId;
}
