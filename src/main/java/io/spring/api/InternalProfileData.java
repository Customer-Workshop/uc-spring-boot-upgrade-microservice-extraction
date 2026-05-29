package io.spring.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternalProfileData {
  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
