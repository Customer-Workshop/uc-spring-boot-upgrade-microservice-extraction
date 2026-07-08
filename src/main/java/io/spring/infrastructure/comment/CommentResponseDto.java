package io.spring.infrastructure.comment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Comment payload returned by the comments microservice. Timestamps are ISO-8601 strings. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentResponseDto {
  private String id;
  private String body;
  private String userId;
  private String articleId;
  private String createdAt;
  private String updatedAt;

  @JsonProperty("author")
  private ProfileDto author;
}
