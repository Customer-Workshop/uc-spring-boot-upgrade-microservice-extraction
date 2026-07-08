package io.spring.comments.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Profile payload returned by the monolith's internal profile endpoint. */
@Data
@NoArgsConstructor
public class ProfileDto {
  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
