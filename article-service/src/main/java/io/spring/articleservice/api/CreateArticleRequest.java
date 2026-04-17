package io.spring.articleservice.api;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateArticleRequest {
  @NotBlank(message = "title can't be empty")
  private String title;

  @NotBlank(message = "description can't be empty")
  private String description;

  @NotBlank(message = "body can't be empty")
  private String body;

  private List<String> tagList;
  private String userId;
}
