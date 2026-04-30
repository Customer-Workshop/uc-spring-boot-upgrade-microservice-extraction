package io.spring.application.article;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("article")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewArticleParam {
  @NotBlank(message = "can't be empty")
  @Size(max = 255, message = "title must be at most 255 characters")
  @DuplicatedArticleConstraint
  private String title;

  @NotBlank(message = "can't be empty")
  private String description;

  @NotBlank(message = "can't be empty")
  private String body;

  private List<String> tagList;
}
