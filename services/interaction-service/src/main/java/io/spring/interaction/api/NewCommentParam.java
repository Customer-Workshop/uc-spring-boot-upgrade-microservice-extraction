package io.spring.interaction.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("comment")
public class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
