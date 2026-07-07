package io.spring.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Mirrors the JSON payload returned by the comments microservice. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentResponse {
  private String id;
  private String body;
  private String userId;
  private String articleId;
  private String createdAt;
  private String updatedAt;
  private AuthorResponse author;

  @Data
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AuthorResponse {
    private String id;
    private String username;
    private String bio;
    private String image;
    private boolean following;
  }
}
