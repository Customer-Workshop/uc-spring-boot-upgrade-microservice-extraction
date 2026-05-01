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
  @Size(max = 255, message = "must be at most 255 characters")
  @DuplicatedArticleConstraint
  private String title;

  @NotBlank(message = "can't be empty")
  @Size(max = 500, message = "must be at most 500 characters")
  private String description;

  @NotBlank(message = "can't be empty")
  @Size(max = 50000, message = "must be at most 50000 characters")
  private String body;

  @Size(max = 10, message = "at most 10 tags allowed")
  private List<String> tagList;
}
