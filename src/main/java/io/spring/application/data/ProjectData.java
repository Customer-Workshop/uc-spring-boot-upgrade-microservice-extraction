package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectData {
  private String id;
  private String name;
  private String description;
  private String client;
  private DateTime startDate;
  private String status;
  private String userId;
  private DateTime createdAt;
  private DateTime updatedAt;
}
