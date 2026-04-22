package io.spring.comments.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
  private String id;
  private String body;
  private String userId;
  private String articleId;
  private DateTime createdAt;
  private DateTime updatedAt;
}
