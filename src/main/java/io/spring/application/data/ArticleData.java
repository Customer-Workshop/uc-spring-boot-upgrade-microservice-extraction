package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.DateTimeCursor;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

/**
 * Read-model DTO for article data, including author profile and engagement metrics.
 *
 * <p>Implements {@link io.spring.application.Node} to support cursor-based pagination in GraphQL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleData implements io.spring.application.Node {
  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private boolean favorited;
  private int favoritesCount;
  private DateTime createdAt;
  private DateTime updatedAt;
  private List<String> tagList;

  @JsonProperty("author")
  private ProfileData profileData;

  @Override
  public DateTimeCursor getCursor() {
    return new DateTimeCursor(updatedAt);
  }
}
