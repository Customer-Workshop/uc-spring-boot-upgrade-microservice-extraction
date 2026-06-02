package io.spring.articleservice.infrastructure.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
  private String id;
  private String username;
  private String bio;
  private String image;
}
