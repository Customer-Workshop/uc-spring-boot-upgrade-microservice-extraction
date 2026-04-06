package io.spring.article.application.article;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("article")
@NoArgsConstructor
@AllArgsConstructor
public class NewArticleParam {
  @NotBlank(message = "can't be empty")
  private String title;

  @NotBlank(message = "can't be empty")
  private String description;

  @NotBlank(message = "can't be empty")
  private String body;

  private List<String> tagList;
}
