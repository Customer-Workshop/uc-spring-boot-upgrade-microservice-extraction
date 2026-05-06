package io.spring.articleservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
  private String id;
  private String email;
  private String username;
  private String bio;
  private String image;
}
