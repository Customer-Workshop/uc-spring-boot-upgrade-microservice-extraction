package io.spring.application.project;

import com.fasterxml.jackson.annotation.JsonRootName;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Getter
@JsonRootName("project")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewProjectParam {
  @NotBlank(message = "can't be empty")
  private String name;

  private String description;

  private String client;

  private DateTime startDate;

  @NotBlank(message = "can't be empty")
  private String status;
}
