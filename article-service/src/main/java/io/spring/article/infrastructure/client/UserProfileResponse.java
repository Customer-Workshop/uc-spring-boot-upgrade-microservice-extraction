package io.spring.article.infrastructure.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileResponse {
  private String id;
  private String username;
  private String bio;
  private String image;
}
