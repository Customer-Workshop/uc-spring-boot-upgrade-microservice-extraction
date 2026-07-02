package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Read-model DTO for user account data (excludes password). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserData {
  private String id;
  private String email;
  private String username;
  private String bio;
  private String image;
}
