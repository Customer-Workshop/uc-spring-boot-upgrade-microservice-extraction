package io.spring.comments.infrastructure.monolith.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
