package io.spring.comments.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.comments.application.DateTimeCursor;
import io.spring.comments.application.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentData implements Node {
  private String id;
  private String body;
  private String articleId;
  private DateTime createdAt;
  private DateTime updatedAt;

  @JsonProperty("author")
  private ProfileData profileData;

  @Override
  public DateTimeCursor getCursor() {
    return new DateTimeCursor(createdAt);
  }
}
