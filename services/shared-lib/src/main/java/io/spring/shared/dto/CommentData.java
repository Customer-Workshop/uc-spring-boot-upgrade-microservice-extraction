package io.spring.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentData {
  private String id;
  private String body;
  @JsonIgnore private String articleId;
  private String createdAt;
  private String updatedAt;

  @JsonProperty("author")
  private ProfileData profileData;
}
