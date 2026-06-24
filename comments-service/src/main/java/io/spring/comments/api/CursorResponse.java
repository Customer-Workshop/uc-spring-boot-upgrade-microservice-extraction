package io.spring.comments.api;

import io.spring.comments.application.data.CommentData;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorResponse {
  private List<CommentData> comments;
  private boolean hasNext;
  private boolean hasPrevious;
  private String startCursor;
  private String endCursor;
}
