package io.spring.comments.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for the profile payload returned by the monolith's internal profile endpoint. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
