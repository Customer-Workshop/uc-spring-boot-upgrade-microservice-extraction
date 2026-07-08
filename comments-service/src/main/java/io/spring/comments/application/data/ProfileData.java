package io.spring.comments.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileData {
  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
