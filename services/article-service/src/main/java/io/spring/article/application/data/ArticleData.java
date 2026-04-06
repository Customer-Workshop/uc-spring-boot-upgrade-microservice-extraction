package io.spring.article.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.shared.dto.ProfileData;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleData {
  private String id;
  @JsonIgnore
  private String userId;
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
}
