package io.spring.infrastructure.comment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Author profile embedded in a comment returned by the comments microservice. */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileDto {
  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
