package io.spring.comments.infrastructure.monolith.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfileResponse {
  private ProfileDTO profile;
}
