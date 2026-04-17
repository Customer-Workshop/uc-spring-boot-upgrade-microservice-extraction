package io.spring.articleservice.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArticleRequest {
  private String title;
  private String description;
  private String body;
}
