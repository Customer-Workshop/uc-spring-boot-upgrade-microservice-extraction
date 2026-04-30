package io.spring.application.article;

import com.fasterxml.jackson.annotation.JsonRootName;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("article")
public class UpdateArticleParam {
  @Size(max = 255, message = "title must be at most 255 characters")
  private String title = "";

  private String body = "";
  private String description = "";
}
