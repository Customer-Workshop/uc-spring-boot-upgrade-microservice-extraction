package io.spring.application.data;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadinessData {
  private RuntimeInfo runtime;
  private List<ReadinessCheck> checks;
  private ReadinessSummary summary;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class RuntimeInfo {
    private String javaVersion;
    private String springBootVersion;
    private String targetJavaVersion;
    private String targetSpringBootVersion;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ReadinessCheck {
    private String id;
    private String title;
    private String status;
    private String detail;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ReadinessSummary {
    private int total;
    private int pass;
    private int fail;
  }
}
