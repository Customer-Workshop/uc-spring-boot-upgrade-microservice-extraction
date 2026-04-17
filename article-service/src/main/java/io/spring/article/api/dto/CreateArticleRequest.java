package io.spring.article.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateArticleRequest {
  @NotBlank private String title;
  @NotBlank private String description;
  @NotBlank private String body;
  private List<String> tagList;
  @NotBlank private String userId;
}
