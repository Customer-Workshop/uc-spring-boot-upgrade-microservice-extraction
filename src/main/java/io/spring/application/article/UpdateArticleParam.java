package io.spring.application.article;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Request payload DTO for updating an existing article. Empty strings indicate no change. */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("article")
public class UpdateArticleParam {
  private String title = "";
  private String body = "";
  private String description = "";
}
