package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadinessRuntimeData {
  private String javaVersion;
  private String springBootVersion;
  private String targetJavaVersion;
  private String targetSpringBootVersion;
}
