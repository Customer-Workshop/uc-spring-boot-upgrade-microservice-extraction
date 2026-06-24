package io.spring.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Enriched comment (with author profile) returned by the comments microservice. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteCommentData {
  private String id;
  private String body;
  private String articleId;
  private String createdAt;
  private String updatedAt;

  @JsonProperty("author")
  private RemoteProfile author;
}
