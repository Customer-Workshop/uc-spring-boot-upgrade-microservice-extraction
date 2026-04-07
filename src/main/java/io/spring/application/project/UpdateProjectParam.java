package io.spring.application.project;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("project")
public class UpdateProjectParam {
  private String name = "";
  private String description = "";
  private String client = "";
  private String startDate = "";
  private String status = "";
}
