package io.spring.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteCursor {
  private List<RemoteCommentData> comments;
  private boolean hasNext;
  private boolean hasPrevious;
  private String startCursor;
  private String endCursor;
}
