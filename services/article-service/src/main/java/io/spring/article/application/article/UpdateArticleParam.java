package io.spring.article.application.article;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("article")
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArticleParam {
  private String title = "";
  private String description = "";
  private String body = "";
}
