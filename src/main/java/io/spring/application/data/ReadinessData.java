package io.spring.application.data;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadinessData {
  private ReadinessRuntimeData runtime;
  private List<ReadinessCheckData> checks;
  private ReadinessSummaryData summary;
}
