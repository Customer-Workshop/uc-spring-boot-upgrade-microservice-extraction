package io.spring.comments.infrastructure.monolith.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfilesResponse {
  private List<ProfileDTO> profiles;
}
