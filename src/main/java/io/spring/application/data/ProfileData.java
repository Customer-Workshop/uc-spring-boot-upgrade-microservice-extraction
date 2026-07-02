package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Read-model DTO for a user's public profile, including the follow status relative to the viewer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileData {
  @JsonIgnore private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
