package io.spring.comments.controller.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {

  @NotBlank(message = "body can't be empty")
  private String body;

  @NotBlank(message = "userId can't be empty")
  private String userId;
}
